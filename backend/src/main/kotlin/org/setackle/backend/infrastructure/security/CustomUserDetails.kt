package org.setackle.backend.infrastructure.security

import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val user: User,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    override fun getPassword(): String = user.getHashedPassword()

    override fun getUsername(): String = user.email.value

    override fun isAccountNonExpired(): Boolean = user.isActive

    override fun isAccountNonLocked(): Boolean = user.isActive

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = user.isActive

    // 추가 사용자 정보 접근 메서드
    fun getUserId(): Long? = user.id?.value

    fun getEmail(): String = user.email.value

    fun getUsernameDisplay(): String = user.username.value

    fun getRole(): UserRole = user.role

    fun getUser(): User = user
}
