package com.example.coderun.domain.auth

import com.example.coderun.domain.users.UserService
import com.example.coderun.util.JwtUtils
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val userService: UserService
) {

    fun registerUser(registerRequest: RegisterRequest): RegisterResponse {
        val passwordHash = passwordEncoder.encode(registerRequest.password)!!

        val registeredUser = userService.createUser(
            registerRequest.email,
            passwordHash,
            registerRequest.firstName,
            registerRequest.lastName,
        )

        return RegisterResponse(
            registeredUser.email,
            registeredUser.firstName,
            registeredUser.lastName,
            registeredUser.createdAt
        )
    }

    fun getAccessTokenDto(request: LoginRequest): LoginResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        val userDetails = authentication.principal as UserDetails
        val accessToken = jwtUtils.generateToken(userDetails)

        return LoginResponse(
            email = request.email,
            accessToken = accessToken,
            expireDate = jwtUtils.extractExpiration(accessToken).toInstant()
        )
    }

}