package com.example.coderun.auth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

   @Test
    fun `should register user, authenticate, and access secured endpoint with JWT`() {
        val user = mapOf(
            "email" to "test_user@example.com",
            "password" to "Pass123$",
            "first_name" to "User",
        )

        // 1. Denied access to secured endpoint without JWT
        mockMvc.get("/api/v1/health-check/test"){
        }.andExpect {
            status { isForbidden() }
        }

        // 2. Register the user
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(user)
        }.andExpect {
            status { isOk() }
        }

        // 3. Login and extract JWT
        val token = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(user)
        }.andExpect {
            status { isOk() }
            jsonPath("$.access_token") { exists() }
        }.andReturn().response.contentAsString.let {
            objectMapper.readTree(it).get("access_token").asString()
        }

        // 4. Access to secured endpoint with JWT
        mockMvc.get("/api/v1/healthcheck/test") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }
}