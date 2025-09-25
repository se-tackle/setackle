package org.setackle.backend.application.security

import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.model.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val user: User
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    override fun getPassword(): String = user.passwordHash

    override fun getUsername(): String = user.email

    override fun isAccountNonExpired(): Boolean = user.deletedAt == null

    override fun isAccountNonLocked(): Boolean = user.isActive

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = user.isActive && user.emailVerified

    // 추가 사용자 정보 접근 메서드
    fun getUserId(): Long? = user.id

    fun getEmail(): String = user.email

    fun getUsernameDisplay(): String = user.username

    fun getRole(): UserRole = user.role

    fun getUser(): User = user
}