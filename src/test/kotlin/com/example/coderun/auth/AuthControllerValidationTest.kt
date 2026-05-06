package com.example.coderun.auth

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.auth.AuthController
import com.example.coderun.domain.auth.AuthService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import jakarta.persistence.EntityExistsException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.test.web.servlet.post
import java.util.stream.Stream

@WebMvcTest(AuthController::class)
class AuthControllerValidationTest : WebSliceTest() {
    @MockkBean
    private lateinit var authService: AuthService

    @ParameterizedTest(name = "Register should fail when {1} is invalid")
    @MethodSource("invalidRegisterProvider")
    fun `register should return 400 for invalid inputs`(
        invalidPayload: Map<String, String?>,
        expectedErrorField: String
    ) {
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidPayload)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.$expectedErrorField") { exists() }
        }
    }

    @ParameterizedTest(name = "Login should fail when {1} is invalid")
    @MethodSource("invalidLoginProvider")
    fun `login should return 400 for invalid inputs`(
        invalidPayload: Map<String, String?>,
        expectedErrorField: String
    ) {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidPayload)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.$expectedErrorField") { exists() }
        }
    }

    @Test
    fun `register should return 409 for already registered email`() {
        val invalidPayload = mapOf(
            "email" to "test@test.com",
            "password" to "Pass123$",
            "first_name" to "User",
        )
        val errorMessage = "User with email \"${invalidPayload["email"]}\" is already registered"

        every {
            authService.registerUser(any())
        } throws EntityExistsException(errorMessage)

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidPayload)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.message") { value(errorMessage) }
        }
    }

    @Test
    fun `login should return 401 for bad credentials`() {
        val invalidPayload = mapOf(
            "email" to "test@test.com",
            "password" to "Pass123$"
        )
        // No user with requested email
        var errorMessage = "User with email: ${invalidPayload["email"]} not found"

        every {
            authService.getAccessTokenDto(any())
        } throws InternalAuthenticationServiceException(errorMessage)

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidPayload)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.message") { value(errorMessage) }
        }

        // Wrong password
        errorMessage = "Bad credentials"

        every {
            authService.getAccessTokenDto(any())
        } throws InternalAuthenticationServiceException(errorMessage)

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidPayload)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.message") { value(errorMessage) }
        }
    }

    companion object {
        @JvmStatic
        fun invalidRegisterProvider(): Stream<Arguments> = Stream.of(
            arguments(mapOf(
                "email" to "bad-email",
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),
            arguments(mapOf(
                "email" to "",
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),
            arguments(mapOf(
                "email" to "a".repeat(256),
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),
            arguments(mapOf(
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),

            arguments(mapOf(
                "password" to "short",
                "email" to "test@test.com", "first_name" to "User"
            ), "password"),
            arguments(mapOf(
                "password" to "a".repeat(256),
                "email" to "test@test.com", "first_name" to "User"
            ), "password"),
            arguments(mapOf(
                "email" to "test@test.com", "first_name" to "User"
            ), "password"),

            arguments(mapOf(
                "first_name" to "",
                "email" to "test@test.com", "password" to "Pass123$"
            ), "first_name"),
            arguments(mapOf(
                "first_name" to "a".repeat(33),
                "email" to "test@test.com", "password" to "Pass123$"
            ), "first_name"),
            arguments(mapOf(
                "email" to "test@test.com", "password" to "Pass123$"
            ), "first_name"),

            arguments(mapOf(
                "last_name" to "",
                "email" to "test@test.com", "password" to "Pass123$", "first_name" to "User",
            ), "last_name"),
            arguments(mapOf(
                "last_name" to "a".repeat(33),
                "email" to "test@test.com", "password" to "Pass123$", "first_name" to "User",
            ), "last_name"),
        )

        @JvmStatic
        fun invalidLoginProvider(): Stream<Arguments> = Stream.of(
            arguments(mapOf(
                "email" to "bad-email",
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),
            arguments(mapOf(
                "email" to "",
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),
            arguments(mapOf(
                "email" to "a".repeat(256),
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),
            arguments(mapOf(
                "password" to "Pass123$", "first_name" to "User"
            ), "email"),

            arguments(mapOf(
                "password" to "short",
                "email" to "test@test.com", "first_name" to "User"
            ), "password"),
            arguments(mapOf(
                "password" to "a".repeat(256),
                "email" to "test@test.com", "first_name" to "User"
            ), "password"),
            arguments(mapOf(
                "email" to "test@test.com", "first_name" to "User"
            ), "password"),
        )
    }
}