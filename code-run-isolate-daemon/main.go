package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
	"os/exec"
	"os/signal"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"syscall"
	"time"
)

type IsolateRequest struct {
	Type          string  `json:"type"`
	Code          string  `json:"code,omitempty"`
	Language      string  `json:"language,omitempty"`
	BinaryPath    string  `json:"binaryPath,omitempty"`
	Input         string  `json:"input,omitempty"`
	TimeLimitSec  float32 `json:"timeLimitSec,omitempty"`
	MemoryLimitKb int     `json:"memoryLimitKb,omitempty"`
}

type IsolateCodeResult struct {
	Status     string  `json:"status"`
	ExitCode   int     `json:"exitCode"`
	ExitSignal int     `json:"exitSignal"`
	Time       float64 `json:"time"`
	Memory     int64   `json:"memory"`
	Stdout     string  `json:"stdout"`
	Stderr     string  `json:"stderr"`
}

type IsolateResponse struct {
	Success    bool               `json:"success"`
	BinaryPath string             `json:"binaryPath,omitempty"`
	Result     *IsolateCodeResult `json:"result,omitempty"`
	Error      string             `json:"error,omitempty"`
}

var boxPool chan int

func init() {
	boxPool = make(chan int, 999)
	for i := 1; i <= 999; i++ {
		boxPool <- i
	}
}

func main() {
	socketPath := os.Getenv("ISOLATE_SOCKET_PATH")
	if socketPath == "" {
		socketPath = "/var/run/isolate-daemon.sock"
	}

	_ = os.Remove(socketPath)
	listener, err := net.Listen("unix", socketPath)
	if err != nil {
		log.Fatalf("Failed to listen on socket %s: %v", socketPath, err)
	}
	defer listener.Close()
	defer os.Remove(socketPath)

	if err := os.Chmod(socketPath, 0777); err != nil {
		log.Printf("Warning: failed to chmod socket: %v", err)
	}

	log.Printf("Listening on %s", socketPath)

	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-sigChan
		log.Println("Shutting down...")
		listener.Close()
		os.Remove(socketPath)
		os.Exit(0)
	}()

	for {
		conn, err := listener.Accept()
		if err != nil {
			log.Printf("Accept error: %v", err)
			continue
		}
		go handleConnection(conn)
	}
}

func handleConnection(conn net.Conn) {
	defer conn.Close()
	scanner := bufio.NewScanner(conn)
	// Need to be able to read large files containing code
	buf := make([]byte, 0, 1024*1024)
	scanner.Buffer(buf, 10*1024*1024)

	for scanner.Scan() {
		line := scanner.Text()
		var req IsolateRequest
		if err := json.Unmarshal([]byte(line), &req); err != nil {
			sendError(conn, fmt.Sprintf("Invalid JSON: %v", err))
			continue
		}

		var res IsolateResponse
		switch req.Type {
		case "compile":
			res = handleCompile(req)
		case "execute":
			res = handleExecute(req)
		case "cleanup":
			res = handleCleanup(req)
		default:
			res = IsolateResponse{Success: false, Error: "Unknown request type: " + req.Type}
		}

		sendResponse(conn, res)
	}
}

func sendResponse(conn net.Conn, res IsolateResponse) {
	data, _ := json.Marshal(res)
	data = append(data, '\n')
	conn.Write(data)
}

func sendError(conn net.Conn, msg string) {
	sendResponse(conn, IsolateResponse{Success: false, Error: msg})
}

func runCommand(cmd ...string) (string, string, error) {
	c := exec.Command(cmd[0], cmd[1:]...)
	var stdout, stderr bytes.Buffer
	c.Stdout = &stdout
	c.Stderr = &stderr
	err := c.Run()
	return stdout.String(), stderr.String(), err
}

func handleCompile(req IsolateRequest) IsolateResponse {
	activeProcessors := os.Getenv("ACTIVE_PROCESSOR_COUNT_FOR_CODE_COMPILATION")
	if activeProcessors == "" {
		activeProcessors = "2"
	}

	tempDir, err := os.MkdirTemp("", "isolate_compile_")
	if err != nil {
		return IsolateResponse{Success: false, Error: "Failed to create temp dir: " + err.Error()}
	}

	var codeFile string
	var binaryPath string
	var compileCmd []string

	switch req.Language {
	case "c":
		codeFile = filepath.Join(tempDir, "code.c")
		binaryPath = filepath.Join(tempDir, "code.bin")
		compileCmd = []string{"gcc", "-O3", codeFile, "-o", binaryPath}
	case "cpp":
		codeFile = filepath.Join(tempDir, "code.cpp")
		binaryPath = filepath.Join(tempDir, "code.bin")
		compileCmd = []string{"g++", "-O3", codeFile, "-o", binaryPath}
	case "python":
		codeFile = filepath.Join(tempDir, "code.py")
		binaryPath = codeFile // no compilation
	case "java":
		uniqueFilename := "Solution"
		codeFile = filepath.Join(tempDir, uniqueFilename+".java")
		re := regexp.MustCompile(`class\s+[a-zA-Z0-9_]+`)
		req.Code = re.ReplaceAllString(req.Code, "class "+uniqueFilename)
		binaryPath = filepath.Join(tempDir, uniqueFilename)
		compileCmd = []string{"sh", "-c", fmt.Sprintf("javac -J-XX:ActiveProcessorCount=%s %s -d %s && native-image -J-XX:ActiveProcessorCount=%s -O3 -cp %s %s -o %s", activeProcessors, codeFile, tempDir, activeProcessors, tempDir, uniqueFilename, binaryPath)}
	case "kotlin":
		uniqueFilename := "code"
		codeFile = filepath.Join(tempDir, uniqueFilename+".kt")
		jarPath := filepath.Join(tempDir, uniqueFilename+".jar")
		binaryPath = filepath.Join(tempDir, uniqueFilename)
		compileCmd = []string{"sh", "-c", fmt.Sprintf("kotlinc -J-XX:ActiveProcessorCount=%s %s -include-runtime -d %s && native-image -J-XX:ActiveProcessorCount=%s -jar %s -o %s", activeProcessors, codeFile, jarPath, activeProcessors, jarPath, binaryPath)}
	default:
		return IsolateResponse{Success: false, Error: "Unsupported language: " + req.Language}
	}

	if err := os.WriteFile(codeFile, []byte(req.Code), 0644); err != nil {
		return IsolateResponse{Success: false, Error: "Failed to write code file: " + err.Error()}
	}

	if len(compileCmd) > 0 {
		_, stderr, err := runCommand(compileCmd...)
		if err != nil {
			return IsolateResponse{Success: false, Error: "Compilation failed: " + err.Error() + " | Stderr: " + stderr}
		}
	}

	return IsolateResponse{Success: true, BinaryPath: binaryPath}
}

func handleExecute(req IsolateRequest) IsolateResponse {
	select {
	case boxId := <-boxPool:
		defer func() { boxPool <- boxId }()

		initOut, _, err := runCommand("sudo", "isolate", "--init", "--cg", fmt.Sprintf("--box-id=%d", boxId))
		if err != nil {
			return IsolateResponse{Success: false, Error: "Failed to init isolate: " + err.Error()}
		}
		boxDir := strings.TrimSpace(initOut) + "/box"

		codeFilename := filepath.Base(req.BinaryPath)
		targetBinPath := filepath.Join(boxDir, codeFilename)

		_, _, err = runCommand("sudo", "cp", req.BinaryPath, targetBinPath)
		if err != nil {
			return IsolateResponse{Success: false, Error: "Failed to copy binary: " + err.Error()}
		}
		runCommand("sudo", "chmod", "777", targetBinPath)

		tempInputFile := filepath.Join(filepath.Dir(req.BinaryPath), "input.txt")
		if err := os.WriteFile(tempInputFile, []byte(req.Input), 0644); err != nil {
			return IsolateResponse{Success: false, Error: "Failed to write input: " + err.Error()}
		}
		runCommand("sudo", "cp", tempInputFile, filepath.Join(boxDir, "input.txt"))

		metaFile := filepath.Join(boxDir, "metadata.txt")
		procs := "1"
		if req.Language == "java" || req.Language == "kotlin" {
			procs = "10"
		}

		cmdArgs := []string{
			"sudo", "isolate", fmt.Sprintf("--box-id=%d", boxId),
			fmt.Sprintf("--meta=%s", metaFile),
			"--stdin=input.txt",
			fmt.Sprintf("--time=%f", req.TimeLimitSec),
			"--cg", fmt.Sprintf("--cg-mem=%d", req.MemoryLimitKb),
			"--processes=" + procs,
			"--dir=/usr/bin/", "--dir=/usr/lib/", "--dir=/lib/", "--dir=/lib64/",
			"--run", "--",
		}

		if req.Language == "python" {
			cmdArgs = append(cmdArgs, "/usr/bin/python3", codeFilename)
		} else {
			cmdArgs = append(cmdArgs, "./"+codeFilename)
		}

		stdout, stderr, _ := runCommand(cmdArgs...)

		metaContent, _, _ := runCommand("sudo", "cat", metaFile)
		meta := parseMeta(metaContent)

		exitCode, _ := strconv.Atoi(meta["exitcode"])
		exitSignal, _ := strconv.Atoi(meta["exitsig"])
		timeUsed, _ := strconv.ParseFloat(meta["time"], 64)
		memoryUsed, _ := strconv.ParseInt(meta["max-rss"], 10, 64)
		status := meta["status"]
		if status == "" {
			status = "OK"
		}

		result := &IsolateCodeResult{
			Status:     status,
			ExitCode:   exitCode,
			ExitSignal: exitSignal,
			Time:       timeUsed,
			Memory:     memoryUsed,
			Stdout:     stdout,
			Stderr:     stderr,
		}

		runCommand("sudo", "isolate", "--cleanup", "--cg", fmt.Sprintf("--box-id=%d", boxId))

		return IsolateResponse{Success: true, Result: result}
	case <-time.After(30 * time.Second):
		return IsolateResponse{Success: false, Error: "Timeout waiting for available isolate box"}
	}
}

func handleCleanup(req IsolateRequest) IsolateResponse {
	if req.BinaryPath != "" {
		os.RemoveAll(filepath.Dir(req.BinaryPath))
	}
	return IsolateResponse{Success: true}
}

func parseMeta(content string) map[string]string {
	meta := make(map[string]string)
	lines := strings.Split(content, "\n")
	for _, line := range lines {
		if line == "" {
			continue
		}
		parts := strings.SplitN(line, ":", 2)
		if len(parts) == 2 {
			meta[parts[0]] = strings.TrimSpace(parts[1])
		}
	}
	return meta
}
