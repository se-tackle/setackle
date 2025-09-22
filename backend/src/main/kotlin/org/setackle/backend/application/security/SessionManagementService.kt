package org.setackle.backend.application.security

import org.setackle.backend.adapter.config.JwtConfig
import org.setackle.backend.domain.user.inbound.*
import org.setackle.backend.domain.user.outbound.TokenCachePort
import org.setackle.backend.domain.user.outbound.UserSessionData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class SessionManagementService(
    private val tokenCachePort: TokenCachePort,
    private val tokenBlacklistUseCase: TokenBlacklistUseCase,
    private val jwtConfig: JwtConfig,
) : ManageSessionUseCase {

    private val logger = LoggerFactory.getLogger(SessionManagementService::class.java)

    companion object {
        private const val MAX_CONCURRENT_SESSIONS = 3 // 최대 동시 세션 수
    }

    override fun createSession(
        userId: Long,
        refreshToken: String,
        deviceInfo: String?,
        ipAddress: String?,
        userAgent: String?,
    ): UserSessionData {
        try {
            val sessionId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()

            val sessionData = UserSessionData(
                userId = userId,
                sessionId = sessionId,
                deviceInfo = deviceInfo,
                ipAddress = ipAddress,
                userAgent = userAgent,
                loginAt = now,
                lastActiveAt = now,
                refreshTokenId = refreshToken,
            )

            // 기존 세션 확인 및 정리
            handleConcurrentSessions(userId, sessionData)

            // 새 세션 저장
            val sessionTtl = Duration.ofSeconds(jwtConfig.refreshTokenValidity)
            tokenCachePort.saveUserSession(userId, sessionData, sessionTtl)

            logger.info("새로운 세션 생성: userId=$userId, sessionId=$sessionId, device=$deviceInfo")
            return sessionData
        } catch (e: Exception) {
            logger.error("세션 생성 중 오류 발생: userId=$userId", e)
            throw SessionManagementException("세션을 생성할 수 없습니다", e)
        }
    }

    override fun getActiveSessions(userId: Long): List<UserSessionData> {
        return try {
            val session = tokenCachePort.getUserSession(userId)
            if (session != null) listOf(session) else emptyList()
        } catch (e: Exception) {
            logger.error("활성 세션 조회 중 오류 발생: userId=$userId", e)
            emptyList()
        }
    }

    override fun updateSessionActivity(userId: Long, sessionId: String): Boolean {
        return try {
            val session = tokenCachePort.getUserSession(userId)
            if (session?.sessionId == sessionId) {
                val now = LocalDateTime.now()
                val timeSinceLastActive = Duration.between(session.lastActiveAt, now)

                // 일정 시간이 지난 경우에만 업데이트 (Redis 부하 감소)
                if (timeSinceLastActive.toMinutes() >= 5) {
                    val updatedSession = session.copy(lastActiveAt = now)
                    val sessionTtl = Duration.ofSeconds(jwtConfig.refreshTokenValidity)
                    tokenCachePort.saveUserSession(userId, updatedSession, sessionTtl)

                    logger.debug("세션 활동 시간 업데이트: userId=$userId, sessionId=$sessionId")
                }
                true
            } else {
                logger.warn("세션 ID가 일치하지 않음: userId=$userId, sessionId=$sessionId")
                false
            }
        } catch (e: Exception) {
            logger.error("세션 활동 시간 업데이트 중 오류 발생: userId=$userId", e)
            false
        }
    }

    override fun invalidateSession(userId: Long, sessionId: String, reason: String): Boolean {
        return try {
            val session = tokenCachePort.getUserSession(userId)
            if (session?.sessionId == sessionId) {
                // Refresh Token 블랙리스트 추가
                tokenBlacklistUseCase.blacklistToken(session.refreshTokenId, reason)

                // 세션 삭제
                tokenCachePort.deleteUserSession(userId)

                logger.info("세션 무효화: userId=$userId, sessionId=$sessionId, reason=$reason")
                true
            } else {
                logger.warn("무효화할 세션을 찾을 수 없음: userId=$userId, sessionId=$sessionId")
                false
            }
        } catch (e: Exception) {
            logger.error("세션 무효화 중 오류 발생: userId=$userId", e)
            false
        }
    }

    override fun invalidateAllUserSessions(userId: Long, reason: String): Int {
        return try {
            val sessions = getActiveSessions(userId)
            var invalidatedCount = 0

            sessions.forEach { session ->
                if (invalidateSession(userId, session.sessionId, reason)) {
                    invalidatedCount++
                }
            }

            logger.info("사용자의 모든 세션 무효화: userId=$userId, count=$invalidatedCount, reason=$reason")
            invalidatedCount
        } catch (e: Exception) {
            logger.error("사용자 전체 세션 무효화 중 오류 발생: userId=$userId", e)
            0
        }
    }

    override fun validateSession(userId: Long, sessionId: String): SessionValidationResult {
        return try {
            val session = tokenCachePort.getUserSession(userId)

            when {
                session == null -> SessionValidationResult(
                    isValid = false,
                    reason = "SESSION_NOT_FOUND",
                )

                session.sessionId != sessionId -> SessionValidationResult(
                    isValid = false,
                    reason = "SESSION_ID_MISMATCH",
                )

                isSessionExpired(session) -> {
                    invalidateSession(userId, sessionId, "SESSION_EXPIRED")
                    SessionValidationResult(
                        isValid = false,
                        reason = "SESSION_EXPIRED",
                    )
                }

                else -> {
                    // 활동 시간 업데이트
                    updateSessionActivity(userId, sessionId)
                    SessionValidationResult(
                        isValid = true,
                        session = session,
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("세션 유효성 검증 중 오류 발생: userId=$userId", e)
            SessionValidationResult(
                isValid = false,
                reason = "VALIDATION_ERROR",
                error = e.message,
            )
        }
    }

    override fun getSessionSummary(userId: Long): SessionSummary {
        return try {
            val sessions = getActiveSessions(userId)

            SessionSummary(
                userId = userId,
                activeSessionCount = sessions.size,
                sessions = sessions.map { session ->
                    SessionInfo(
                        sessionId = session.sessionId,
                        deviceInfo = session.deviceInfo,
                        ipAddress = session.ipAddress,
                        loginAt = session.loginAt,
                        lastActiveAt = session.lastActiveAt,
                        isCurrentSession = true, // 현재는 단일 세션만 지원
                    )
                },
                lastActivity = sessions.maxByOrNull { it.lastActiveAt }?.lastActiveAt,
            )
        } catch (e: Exception) {
            logger.error("세션 요약 조회 중 오류 발생: userId=$userId", e)
            SessionSummary(
                userId = userId,
                activeSessionCount = 0,
                sessions = emptyList(),
                lastActivity = null,
                error = e.message,
            )
        }
    }

    override fun cleanupInactiveSessions(): Long {
        // Redis TTL에 의해 자동으로 정리되므로 별도 구현 불필요
        return 0L
    }

    private fun handleConcurrentSessions(userId: Long, newSession: UserSessionData) {
        try {
            val existingSessions = getActiveSessions(userId)

            // 동일한 디바이스에서의 로그인인 경우 기존 세션 무효화
            val sameDeviceSessions = existingSessions.filter { session ->
                session.deviceInfo == newSession.deviceInfo &&
                    session.ipAddress == newSession.ipAddress
            }

            sameDeviceSessions.forEach { session ->
                invalidateSession(userId, session.sessionId, "SAME_DEVICE_LOGIN")
            }

            // 최대 세션 수 초과 시 가장 오래된 세션 무효화
            val remainingSessions = existingSessions - sameDeviceSessions.toSet()
            if (remainingSessions.size >= MAX_CONCURRENT_SESSIONS) {
                val sessionsToRemove = remainingSessions
                    .sortedBy { it.lastActiveAt }
                    .take(remainingSessions.size - MAX_CONCURRENT_SESSIONS + 1)

                sessionsToRemove.forEach { session ->
                    invalidateSession(userId, session.sessionId, "MAX_SESSIONS_EXCEEDED")
                }
            }
        } catch (e: Exception) {
            logger.error("동시 세션 처리 중 오류 발생: userId=$userId", e)
        }
    }

    private fun isSessionExpired(session: UserSessionData): Boolean {
        val now = LocalDateTime.now()
        val sessionDuration = Duration.between(session.loginAt, now)
        val maxSessionDuration = Duration.ofSeconds(jwtConfig.refreshTokenValidity)

        return sessionDuration > maxSessionDuration
    }
}
