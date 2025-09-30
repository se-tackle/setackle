package org.setackle.backend.application.user.outbound

import org.setackle.backend.domain.user.vo.Email
import org.setackle.backend.domain.user.model.User
import org.setackle.backend.domain.user.vo.UserId
import org.setackle.backend.domain.user.vo.Username

/**
 * 사용자 관련 출력 포트 (Output Port)
 */
interface UserPort {

    /**
     * 사용자 조회
     */
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun findByUsername(username: Username): User?

    /**
     * 사용자 존재 여부 확인
     */
    fun existsByEmail(email: Email): Boolean
    fun existsByUsername(username: Username): Boolean

    /**
     * 사용자 저장
     */
    fun save(user: User): User

    /**
     * 사용자 삭제 (하드 삭제)
     */
    fun delete(user: User)

    /**
     * 사용자 계정 삭제 표시 (소프트 삭제)
     */
    fun markAsDeleted(user: User): User
}
