package com.example.coderun.domain.users

import com.example.coderun.config.ValidatesInput
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
class UserController(
    private val userService: UserService,
    private val fileStorageService: FileStorageService
) {

    private fun getCurrentUser(): User {
        return SecurityContextHolder.getContext().authentication!!.principal as User
    }

    @GetMapping("/me")
    fun getMyProfile(): ResponseEntity<UserProfileDto> {
        val user = getCurrentUser()
        return ResponseEntity.ok(userService.getUserProfile(user))
    }

    @PutMapping("/me")
    @ValidatesInput
    fun updateMyProfile(@Valid @RequestBody request: UpdateProfileRequest): ResponseEntity<UserProfileDto> {
        val user = getCurrentUser()
        return ResponseEntity.ok(userService.updateProfile(user, request))
    }

    @PostMapping("/me/avatar")
    fun uploadAvatar(@RequestParam("file") file: MultipartFile): ResponseEntity<UserProfileDto> {
        val user = getCurrentUser()
        
        // Delete old avatar if exists
        user.photoUrl?.let {
            fileStorageService.deleteAvatar(it)
        }

        val photoUrl = fileStorageService.storeAvatar(file)
        return ResponseEntity.ok(userService.updateAvatar(user, photoUrl))
    }

    @DeleteMapping("/me/avatar")
    fun deleteAvatar(): ResponseEntity<UserProfileDto> {
        val user = getCurrentUser()
        
        user.photoUrl?.let {
            fileStorageService.deleteAvatar(it)
        }
        
        return ResponseEntity.ok(userService.updateAvatar(user, null))
    }
}
