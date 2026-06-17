package com.example.coderun.domain.home.controller

import com.example.coderun.domain.home.dto.HomeRecommendationsDto
import com.example.coderun.domain.home.service.HomeRecommendationService
import com.example.coderun.domain.users.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/home")
class HomeRecommendationController(
    private val homeRecommendationService: HomeRecommendationService
) {

    @GetMapping("/recommendations")
    fun getRecommendations(): ResponseEntity<HomeRecommendationsDto> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = if (auth != null && auth.principal is User) {
            auth.principal as User
        } else {
            null
        }
        return ResponseEntity.ok(homeRecommendationService.getRecommendations(user?.id))
    }
}
