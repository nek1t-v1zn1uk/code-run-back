package com.example.coderun.domain.users

import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
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

    @Column(nullable = false, columnDefinition = "user_roles")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var role: UserRoles = UserRoles.USER,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_" + role.name))
    }

    override fun getPassword() = passwordHash

    override fun getUsername() = email

}
