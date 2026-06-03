package com.example.coderun.domain.users

import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String) = loadUserByEmail(username)

    fun loadUserByEmail(email: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("User with email: $email not found")
        return user
    }

    fun createUser(
        email: String,
        passwordHash: String,
        firstName: String,
        lastName: String? = null,
        photoUrl: String? = null
    ): User {
        if(userRepository.findByEmail(email) != null) {
            throw EntityExistsException("User with email \"$email\" is already registered")
        }

        val newUser = User(
            email = email,
            passwordHash = passwordHash,
            firstName = firstName,
            lastName = lastName,
            photoUrl = photoUrl,
        )
        return userRepository.save(newUser)
    }

    fun getUserProfile(user: User): UserProfileDto {
        return UserProfileDto(
            id = user.id!!,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            photoUrl = user.photoUrl,
            role = user.role.name,
            createdAt = user.createdAt
        )
    }

    fun updateProfile(user: User, request: UpdateProfileRequest): UserProfileDto {
        user.firstName = request.firstName
        user.lastName = request.lastName
        val updatedUser = userRepository.save(user)
        return getUserProfile(updatedUser)
    }

    fun updateAvatar(user: User, photoUrl: String?): UserProfileDto {
        user.photoUrl = photoUrl
        val updatedUser = userRepository.save(user)
        return getUserProfile(updatedUser)
    }

}