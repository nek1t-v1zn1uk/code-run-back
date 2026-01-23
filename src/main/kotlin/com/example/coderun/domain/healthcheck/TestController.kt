package com.example.coderun.domain.healthcheck

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/healthcheck")
@Tag(name = "Server Healthcheck")
class TestController {
    @GetMapping("/test")
    fun testHelloWorld(): ResponseEntity<*> {
        return ResponseEntity.ok("Hello World")
    }
}