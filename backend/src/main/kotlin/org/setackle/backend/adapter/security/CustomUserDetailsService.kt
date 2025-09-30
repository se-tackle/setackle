package org.setackle.backend.adapter.security

import org.setackle.backend.adapter.persistence.user.repository.UserJpaRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userJpaRepository: UserJpaRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val userEntity = userJpaRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $email")

        return CustomUserDetails(userEntity.toDomain())
    }

    fun loadUserByUserId(userId: Long): UserDetails {
        val userEntity = userJpaRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다: $userId") }

        return CustomUserDetails(userEntity.toDomain())
    }
}