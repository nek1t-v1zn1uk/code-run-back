package com.example.coderun.domain.users

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
