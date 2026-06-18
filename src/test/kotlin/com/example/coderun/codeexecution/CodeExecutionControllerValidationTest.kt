package com.example.coderun.codeexecution

import com.example.coderun.WebSliceTest
import com.example.coderun.domain.codeexecution.CodeExecutionController
import com.example.coderun.domain.codeexecution.CodeExecutionRequest
import com.example.coderun.domain.codeexecution.CodeExecutionResponse
import com.example.coderun.domain.codeexecution.CodeExecutionService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CodeExecutionController::class)
@AutoConfigureMockMvc(addFilters = false)
class CodeExecutionControllerValidationTest : WebSliceTest() {

    @MockkBean
    private lateinit var codeExecutionService: CodeExecutionService

    @Test
    fun `run code should return execution results on success`() {
        val request = CodeExecutionRequest(
            code = "print('hello')",
            input = "some-input",
            language = "python"
        )
        val expectedResponse = CodeExecutionResponse(
            status = "OK",
            exitCode = 0,
            output = "hello\n",
            time = 0.05,
            memory = 1024,
            error = ""
        )

        every { codeExecutionService.runCode(any()) } returns expectedResponse

        mockMvc.perform(post("/api/v1/code-execution")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.output").value("hello\n"))
            .andExpect(jsonPath("$.exit_code").value(0))

        verify { codeExecutionService.runCode(any()) }
    }
}
