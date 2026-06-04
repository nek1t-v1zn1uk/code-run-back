package com.example.coderun.domain.isolaterunner

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter

@Service
class IsolateService(
    @Value("\${app.isolate-socket-path:/var/run/isolate-daemon.sock}")
    private val socketPath: String
) {
    private val mapper = jacksonObjectMapper()

    private fun sendRequest(request: IsolateRequest): IsolateResponse {
        val socketFile = File(socketPath)
        if (!socketFile.exists()) {
            throw RuntimeException("Isolate socket not found at $socketPath")
        }

        AFUNIXSocket.newInstance().use { socket ->
            socket.connect(AFUNIXSocketAddress.of(socketFile))
            val writer = PrintWriter(socket.outputStream, true)
            val reader = BufferedReader(InputStreamReader(socket.inputStream))

            val jsonReq = mapper.writeValueAsString(request)
            writer.println(jsonReq)

            val jsonRes = reader.readLine()
                ?: throw RuntimeException("Empty response from isolate daemon")
            
            val res = mapper.readValue(jsonRes, IsolateResponse::class.java)
            if (!res.success) {
                throw RuntimeException("Isolate error: ${res.error}")
            }
            return res
        }
    }

    fun compileCode(code: String, language: String): String {
        val req = IsolateRequest(
            type = "compile",
            code = code,
            language = language
        )
        val res = sendRequest(req)
        return res.binaryPath ?: throw RuntimeException("No binary path returned")
    }

    fun executeCode(
        code: String,
        language: String,
        input: String = "",
        timeInSec: Float = 1.0F,
        memoryInKB: Int = 256_000
    ): IsolateCodeResult {
        val binaryPath = compileCode(code, language)
        try {
            return executeCompiledCode(binaryPath, language, input, timeInSec, memoryInKB)
        } finally {
            cleanupBinary(binaryPath)
        }
    }

    fun executeCompiledCode(
        binaryPath: String,
        language: String,
        input: String = "",
        timeInSec: Float = 1.0F,
        memoryInKB: Int = 256_000
    ): IsolateCodeResult {
        val req = IsolateRequest(
            type = "execute",
            binaryPath = binaryPath,
            language = language,
            input = input,
            timeLimitSec = timeInSec,
            memoryLimitKb = memoryInKB
        )
        val res = sendRequest(req)
        return res.result ?: throw RuntimeException("No result returned from isolate daemon")
    }

    fun cleanupBinary(binaryPath: String) {
        try {
            sendRequest(IsolateRequest(type = "cleanup", binaryPath = binaryPath))
        } catch (e: Exception) {
            // Ignore cleanup errors
            e.printStackTrace()
        }
    }
}