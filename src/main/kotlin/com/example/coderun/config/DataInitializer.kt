package com.example.coderun.config

import com.example.coderun.domain.users.User
import com.example.coderun.domain.users.UserRepository
import com.example.coderun.domain.users.UserRoles
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer {

    @Value("\${app.admin.email}")
    lateinit var adminEmail: String

    @Value("\${app.admin.password}")
    lateinit var adminPassword: String

    @Bean
    fun initAdminUser(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): CommandLineRunner {
        return CommandLineRunner {
            if (userRepository.findByEmail(adminEmail) == null) {
                val admin = User(
                    email = adminEmail,
                    passwordHash = passwordEncoder.encode(adminPassword)!!,
                    firstName = "System",
                    lastName = "Administrator",
                    role = UserRoles.ADMIN
                )
                userRepository.save(admin)
                println("=====================================================")
                println("Created default ADMIN user: $adminEmail")
                println("=====================================================")
            }
        }
    }
}
