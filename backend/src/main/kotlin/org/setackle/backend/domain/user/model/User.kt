package org.setackle.backend.domain.user.model

import org.setackle.backend.domain.user.vo.*
import java.time.LocalDateTime

/**
 * 사용자 도메인 모델
 * 사용자와 관련된 모든 비즈니스 로직을 캡슐화
 */
class User private constructor(
    val id: UserId?,
    val email: Email,
    var username: Username,
    private var password: Password,
    var role: UserRole,
    private var _isActive: Boolean,
    private var _emailVerified: Boolean,
    var registeredAt: LocalDateTime? = null,
    var lastLoginAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
    var deletionReason: String? = null,
) {

    /**
     * 계정 활성 상태 (읽기 전용)
     */
    val isActive: Boolean get() = _isActive

    /**
     * 이메일 인증 상태 (읽기 전용)
     */
    val emailVerified: Boolean get() = _emailVerified

    /**
     * 계정 삭제 상태 (읽기 전용)
     */
    val isDeleted: Boolean get() = deletedAt != null

    /**
     * 사용자 등록 팩토리 메소드
     */
    companion object {
        fun register(
            email: Email,
            username: Username,
            password: Password,
            role: UserRole = UserRole.USER
        ): User {
            val user = User(
                id = UserId.newUser(),
                email = email,
                username = username,
                password = password,
                role = role,
                _isActive = true,
                _emailVerified = false
            )

            return user
        }

        /**
         * 기존 데이터로부터 User 생성 (영속성 계층용)
         */
        fun reconstruct(
            id: UserId?,
            email: Email,
            username: Username,
            password: Password,
            role: UserRole,
            isActive: Boolean,
            emailVerified: Boolean,
            registeredAt: LocalDateTime,
            lastLoginAt: LocalDateTime? = null,
            deletedAt: LocalDateTime? = null,
            deletionReason: String? = null,
        ): User {
            return User(
                id = id,
                email = email,
                username = username,
                password = password,
                role = role,
                _isActive = isActive,
                _emailVerified = emailVerified,
                registeredAt = registeredAt,
                lastLoginAt = lastLoginAt,
                deletedAt = deletedAt,
                deletionReason = deletionReason,
            )
        }
    }

    /**
     * 계정 활성화
     */
    fun activate() {
        require(!_isActive) { "이미 활성화된 계정입니다." }

        _isActive = true
    }

    /**
     * 계정 비활성화
     */
    fun deactivate(reason: String) {
        require(_isActive) { "이미 비활성화된 계정입니다." }
        require(reason.isNotBlank()) { "비활성화 사유는 필수입니다." }

        _isActive = false
    }

    /**
     * 이메일 인증 완료
     */
    fun verifyEmail() {
        require(!_emailVerified) { "이미 인증된 이메일입니다." }
        require(_isActive) { "비활성화된 계정의 이메일은 인증할 수 없습니다." }

        _emailVerified = true
    }

    /**
     * 비밀번호 변경
     */
    fun changePassword(newPassword: Password) {
        require(_isActive) { "비활성화된 계정의 비밀번호는 변경할 수 없습니다." }

        password = newPassword
    }

    /**
     * 로그인 기록
     */
    fun recordLogin() {
        require(_isActive) { "비활성화된 계정으로는 로그인할 수 없습니다." }

        lastLoginAt = LocalDateTime.now()
    }

    /**
     * 프로필 업데이트
     */
    fun updateProfile(newUsername: Username) {
        require(_isActive) { "비활성화된 계정의 프로필은 수정할 수 없습니다." }

        username = newUsername
    }

    /**
     * 비밀번호 매치 확인
     */
    fun matchesPassword(rawPassword: String, encoder: (String) -> String): Boolean {
        return password.matches(rawPassword, encoder)
    }

    /**
     * 로그인 가능 여부 확인
     */
    fun canLogin(): Boolean {
        return _isActive
    }

    /**
     * 특정 기능 접근 가능 여부 (이메일 인증 필요)
     */
    fun canAccessVerifiedFeatures(): Boolean {
        return _isActive && _emailVerified && !isDeleted
    }

    /**
     * 계정 삭제 처리 (소프트 삭제)
     */
    fun markAsDeleted(reason: String? = null) {
        require(!isDeleted) { "이미 삭제된 계정입니다." }

        deletedAt = LocalDateTime.now()
        deletionReason = reason

        // 삭제와 함께 계정 비활성화
        _isActive = false
    }

    /**
     * 개인정보 익명화 처리
     */
    fun anonymize(): User {
        val anonymizedEmail = Email.of("deleted.user.${System.currentTimeMillis()}@deleted.com")
        val anonymizedUsername = Username.of("deleted_user_${System.currentTimeMillis()}")

        return User(
            id = id,
            email = anonymizedEmail,
            username = anonymizedUsername,
            password = password,
            role = role,
            _isActive = false,
            _emailVerified = false,
            registeredAt = registeredAt,
            lastLoginAt = lastLoginAt,
            deletedAt = deletedAt,
            deletionReason = deletionReason,
        )
    }

    /**
     * 저장 후 실제 ID로 업데이트 (영속성 계층에서 호출)
     */
    fun updateIdAfterPersistence(newId: UserId): User {
        return User(
            id = newId,
            email = email,
            username = username,
            password = password,
            role = role,
            _isActive = _isActive,
            _emailVerified = _emailVerified,
            registeredAt = registeredAt,
            lastLoginAt = lastLoginAt,
            deletedAt = deletedAt,
            deletionReason = deletionReason,
        )
    }

    /**
     * 해싱된 비밀번호 반환 (영속성 계층용)
     */
    fun getHashedPassword(): String {
        return password.hashedValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return email == other.email
    }

    override fun hashCode(): Int = email.hashCode()

    override fun toString(): String =
        "User(id=$id, email=$email, username='$username', role=$role, isActive=$isActive)"
}