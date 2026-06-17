package com.example.coderun

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableCaching
class CoderunApplication

fun main(args: Array<String>) {
    runApplication<CoderunApplication>(*args)
}
