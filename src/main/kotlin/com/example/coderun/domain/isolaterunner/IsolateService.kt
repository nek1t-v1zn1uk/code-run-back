package com.example.coderun.domain.isolaterunner

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

@Service
class IsolateService {
    private val activeProcessorCount = System.getenv("ACTIVE_PROCESSOR_COUNT_FOR_CODE_COMPILATION") ?: 2
    val boxIds = ArrayDeque<Int>().apply { addAll(1..999) }

    fun executeCode(
        code: String,
        language: String,
        input: String = "",
        timeInSec: Float = 1.0F,
        memoryInKB: Int = 256_000
    ): IsolateCodeResult {
        val boxId = boxIds.removeFirstOrNull()
            ?: throw IndexOutOfBoundsException("No available box IDs left")

        val boxDir = "/var/lib/isolate/$boxId/box"

        // initialize box
        runCommand("sudo isolate --init --box-id=$boxId")

        // create needed files in box
        val tempCodeFile = createCodeFile(code, language)
        val codeFilename = tempCodeFile.fileName.toString()
        runCommand("sudo cp $tempCodeFile $boxDir/$codeFilename")
        val tempInputFile = Files.createTempFile("input", ".txt")
        Files.writeString(tempInputFile, input)
        runCommand("sudo cp $tempInputFile $boxDir/input.txt")

        runCommand("sudo chmod 777 $boxDir/$codeFilename")

        // run code
        val command = buildList {
            addAll(listOf(
                "sudo", "isolate", "--box-id=$boxId",
                "--meta=$boxDir/metadata.txt",
                "--stdin=input.txt",
                "--time=$timeInSec",
                "--mem=$memoryInKB",
                "--processes=${
                    if(language.startsWith("kotlin")) 10
                    else 1
                }",
                "--dir=/usr/bin/", "--dir=/usr/lib/", "--dir=/lib/", "--dir=/lib64/",
                "--run", "--"
            ))
            if(language.startsWith("python")){
                add("/usr/bin/python3")
                add(codeFilename)
            }
            else
                add("./$codeFilename") // for binaries
        }

        val process = ProcessBuilder(command).start()
        process.waitFor(5, TimeUnit.SECONDS)

        // get results
        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val metaContent = ProcessBuilder("sudo", "cat", "$boxDir/metadata.txt")
            .start()
            .inputStream.bufferedReader().readText()
        val metaData = parseMeta(metaContent)

        val result = IsolateCodeResult(
            status = metaData["status"] ?: "OK",
            exitCode = metaData["exitcode"]?.toInt() ?: 0,
            time = metaData["time"]?.toDouble() ?: 0.0,
            memory = metaData["max-rss"]?.toLong() ?: 0L,
            stdout = stdout,
            stderr = stderr
        )

        // clear the box
        runCommand("sudo isolate --cleanup --box-id=$boxId")
        Files.deleteIfExists(tempInputFile)
        Files.deleteIfExists(tempCodeFile)
        boxIds.addLast(boxId)

        return result
    }

    private fun runCommand(cmd: String) {
        val cmdInTokens = cmd.split(" ")
        ProcessBuilder(cmdInTokens).start().waitFor()
    }

    private fun parseMeta(content: String): Map<String, String> {
        return content.lines() // divide to list of rows
            .filter { it.isNotBlank() } // remove empty rows
            .associate { line ->
                val parts = line.split(":", limit = 2)
                parts[0] to parts[1]
            }
    }

    private fun createCodeFile(code: String, language: String): Path{
        lateinit var tempCodeFile: Path
        if(language.startsWith("python")) {
            tempCodeFile = Files.createTempFile("code", ".py")
            Files.writeString(tempCodeFile, code)
        } else if (language.startsWith("kotlin")) {
            // create .kt file
            tempCodeFile = Files.createTempFile("code", ".kt")
            Files.writeString(tempCodeFile, code)

            val parentDir = tempCodeFile.parent.absolutePathString()
            val jarPath = "$parentDir/code.jar"
            val binaryPath = "$parentDir/code"

            // compile to .jar
            runCommand("kotlinc -J-XX:ActiveProcessorCount=$activeProcessorCount ${tempCodeFile.absolutePathString()} -include-runtime -d $jarPath")

            // compile to native binary
            runCommand("native-image -J-XX:ActiveProcessorCount=$activeProcessorCount -jar $jarPath -o $binaryPath")

            Files.deleteIfExists(tempCodeFile)
            Files.deleteIfExists(Path.of(jarPath))

            tempCodeFile = Path.of(binaryPath)
        } else
            throw NoSuchMethodException("No such language: $language")

        return tempCodeFile
    }
}