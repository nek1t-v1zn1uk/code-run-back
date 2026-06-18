package com.example.coderun.users

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.users.*
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.stream.Stream

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for pure web slice testing
class UserControllerValidationTest : WebSliceTest() {

    @MockkBean
    private lateinit var fileStorageService: FileStorageService

    private lateinit var mockUser: User

    @BeforeEach
    fun setupSecurityContext() {
        mockUser = User(
            email = "test@example.com",
            passwordHash = "hash",
            firstName = "John",
            lastName = "Doe",
            role = UserRoles.USER
        )
        mockUser.id = 1
        val authentication = UsernamePasswordAuthenticationToken(mockUser, null, mockUser.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `get profile should return success`() {
        val expectedDto = UserProfileDto(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            photoUrl = null,
            role = "USER",
            createdAt = Instant.now()
        )

        every { userService.getUserProfile(any()) } returns expectedDto

        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.first_name").value("John"))
    }

    @Test
    fun `update profile should return success when valid`() {
        val request = UpdateProfileRequest(firstName = "Jane", lastName = "Smith")
        val expectedDto = UserProfileDto(
            id = 1,
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Smith",
            photoUrl = null,
            role = "USER",
            createdAt = Instant.now()
        )

        every { userService.updateProfile(any(), any()) } returns expectedDto

        mockMvc.perform(put("/api/v1/users/me")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.first_name").value("Jane"))
            .andExpect(jsonPath("$.last_name").value("Smith"))
    }

    @ParameterizedTest(name = "Update Profile should fail when {1} is invalid")
    @MethodSource("invalidUpdateProfileProvider")
    fun `update profile should return 400 for invalid inputs`(
        invalidPayload: Map<String, String?>,
        expectedErrorField: String
    ) {
        mockMvc.perform(put("/api/v1/users/me")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPayload)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.$expectedErrorField").exists())
    }

    @Test
    fun `upload avatar should return success`() {
        val file = MockMultipartFile("file", "avatar.png", "image/png", "test-image".toByteArray())
        val photoUrl = "avatars/uuid.png"
        val expectedDto = UserProfileDto(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            photoUrl = photoUrl,
            role = "USER",
            createdAt = Instant.now()
        )

        every { fileStorageService.storeAvatar(any()) } returns photoUrl
        every { userService.updateAvatar(any(), photoUrl) } returns expectedDto

        mockMvc.perform(multipart("/api/v1/users/me/avatar").file(file))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.photo_url").value(photoUrl))

        verify { fileStorageService.storeAvatar(any()) }
        verify { userService.updateAvatar(any(), photoUrl) }
    }

    @Test
    fun `delete avatar should return success`() {
        val expectedDto = UserProfileDto(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            photoUrl = null,
            role = "USER",
            createdAt = Instant.now()
        )

        // Give the mock user an existing avatar to trigger deletion
        mockUser.photoUrl = "avatars/old.png"

        every { fileStorageService.deleteAvatar(any()) } returns Unit
        every { userService.updateAvatar(any(), null) } returns expectedDto

        mockMvc.perform(delete("/api/v1/users/me/avatar"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.photo_url").doesNotExist())

        verify { fileStorageService.deleteAvatar("avatars/old.png") }
        verify { userService.updateAvatar(any(), null) }
    }

    @Test
    fun `change password should return success when valid`() {
        val request = ChangePasswordRequest(oldPassword = "OldPassword123", newPassword = "NewPassword123")

        every { userService.changePassword(any(), any()) } returns Unit

        mockMvc.perform(post("/api/v1/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)

        verify { userService.changePassword(any(), any()) }
    }

    @ParameterizedTest(name = "Change Password should fail when {1} is invalid")
    @MethodSource("invalidChangePasswordProvider")
    fun `change password should return 400 for invalid inputs`(
        invalidPayload: Map<String, String?>,
        expectedErrorField: String
    ) {
        mockMvc.perform(post("/api/v1/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPayload)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.$expectedErrorField").exists())
    }

    companion object {
        @JvmStatic
        fun invalidUpdateProfileProvider(): Stream<Arguments> = Stream.of(
            arguments(mapOf("first_name" to ""), "first_name"),
            arguments(mapOf("first_name" to "a".repeat(33)), "first_name"),
            arguments(mapOf("first_name" to "ValidName", "last_name" to ""), "last_name"),
            arguments(mapOf("first_name" to "ValidName", "last_name" to "a".repeat(33)), "last_name")
        )

        @JvmStatic
        fun invalidChangePasswordProvider(): Stream<Arguments> = Stream.of(
            arguments(mapOf("old_password" to "", "new_password" to "ValidPass123"), "old_password"),
            arguments(mapOf("old_password" to "ValidOldPass", "new_password" to ""), "new_password"),
            arguments(mapOf("old_password" to "ValidOldPass", "new_password" to "short"), "new_password"),
            arguments(mapOf("old_password" to "ValidOldPass", "new_password" to "a".repeat(65)), "new_password")
        )
    }
}
