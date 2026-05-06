package com.example.coderun.domain.auth

import com.example.coderun.config.ValidatesInput
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ValidatesInput
    @ApiResponse(responseCode = "409", description = "Email is already registered")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        return ResponseEntity.ok(authService.registerUser(request))
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ValidatesInput
    @ApiResponse(responseCode = "401", description = "Wrong email or password")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authService.getAccessTokenDto(request))
    }

}