package com.example.coderun

import com.example.coderun.domain.users.UserService
import com.example.coderun.util.JwtUtils
import com.ninjasquad.springmockk.MockkBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import tools.jackson.databind.ObjectMapper

@WebMvcTest
abstract class WebSliceTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc
    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockkBean
    protected lateinit var jwtUtils: JwtUtils
    @MockkBean
    protected lateinit var userService: UserService
}