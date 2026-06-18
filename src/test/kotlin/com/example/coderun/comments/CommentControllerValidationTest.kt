package com.example.coderun.comments

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.comments.controller.CommentController
import com.example.coderun.domain.comments.dto.CommentDto
import com.example.coderun.domain.comments.dto.CreateCommentDto
import com.example.coderun.domain.comments.service.CommentService
import com.example.coderun.domain.users.User
import com.example.coderun.domain.users.UserRoles
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Instant

@WebMvcTest(CommentController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CommentControllerValidationTest.TestConfig::class)
class CommentControllerValidationTest : WebSliceTest() {

    @TestConfiguration
    class TestConfig : WebMvcConfigurer {
        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(AuthenticationPrincipalArgumentResolver())
        }
    }

    @MockkBean
    private lateinit var commentService: CommentService

    private lateinit var mockUser: User

    @BeforeEach
    fun setupSecurityContext() {
        mockUser = User(
            email = "commenter@example.com",
            passwordHash = "hash",
            firstName = "John",
            lastName = "Comment",
            role = UserRoles.USER
        )
        mockUser.id = 999
        val authentication = UsernamePasswordAuthenticationToken(mockUser, null, mockUser.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `get comments should return comment list`() {
        val comments = listOf(
            CommentDto(
                id = 1,
                problemId = 10,
                userId = 999,
                userFirstName = "John",
                userLastName = "Comment",
                userPhotoUrl = null,
                text = "Nice problem!",
                pinnedSolutionId = null,
                createdAt = Instant.now(),
                replies = emptyList()
            )
        )

        every { commentService.getCommentsByProblem(10) } returns comments

        mockMvc.perform(get("/api/v1/problems/10/comments"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].text").value("Nice problem!"))
            .andExpect(jsonPath("$[0].user_first_name").value("John"))
    }

    @Test
    fun `add comment should return comment on success`() {
        val request = CreateCommentDto(text = "My comment text", parentId = null, pinnedSolutionId = null)
        val expectedDto = CommentDto(
            id = 2,
            problemId = 10,
            userId = 999,
            userFirstName = "John",
            userLastName = "Comment",
            userPhotoUrl = null,
            text = "My comment text",
            pinnedSolutionId = null,
            createdAt = Instant.now(),
            replies = emptyList()
        )

        every { commentService.addComment(10, 999, any()) } returns expectedDto

        mockMvc.perform(post("/api/v1/problems/10/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.text").value("My comment text"))

        verify { commentService.addComment(10, 999, any()) }
    }
}
