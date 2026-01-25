package com.example.coderun.domain.isolaterunner

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.util.Queue
import java.util.concurrent.TimeUnit

@Service
class IsolateService {
    val boxIds = ArrayDeque<Int>().apply { addAll(1..999) }

    fun executeCode(
        code: String,
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
        val tempCodeFile = Files.createTempFile("code", ".py")
        Files.writeString(tempCodeFile, code)
        runCommand("sudo cp $tempCodeFile ${boxDir}/code.py")
        val tempInputFile = Files.createTempFile("input", ".txt")
        Files.writeString(tempInputFile, input)
        runCommand("sudo cp $tempInputFile ${boxDir}/input.txt")

        runCommand("sudo chmod 644 $boxDir/code.py")

        // run code
        val command = listOf(
            "sudo", "isolate", "--box-id=$boxId",
            "--meta=$boxDir/metadata.txt",
            "--stdin=input.txt",
            "--time=$timeInSec",
            "--mem=$memoryInKB",
            "--dir=/usr/bin/", "--dir=/usr/lib/", "--dir=/lib/",
            "--run", "--", "/usr/bin/python3", "code.py"
        )
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
}