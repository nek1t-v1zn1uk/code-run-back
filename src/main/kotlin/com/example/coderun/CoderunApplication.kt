package com.example.coderun

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoderunApplication

fun main(args: Array<String>) {
    runApplication<CoderunApplication>(*args)
}
