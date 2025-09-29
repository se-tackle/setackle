package org.setackle.backend.domain.user.model

/**
 * 세션 정보 값 객체
 */
data class SessionInfo(
    val deviceInfo: String?,
    val ipAddress: String?,
    val userAgent: String?
) {
    companion object {
        fun of(deviceInfo: String?, ipAddress: String?, userAgent: String?): SessionInfo {
            return SessionInfo(
                deviceInfo = deviceInfo?.takeIf { it.isNotBlank() },
                ipAddress = ipAddress?.takeIf { it.isNotBlank() },
                userAgent = userAgent?.takeIf { it.isNotBlank() }
            )
        }

        fun empty(): SessionInfo {
            return SessionInfo(null, null, null)
        }
    }

    /**
     * 기기 타입 추출
     */
    fun getDeviceType(): String? {
        return deviceInfo?.let { userAgent ->
            when {
                userAgent.contains("Mobile", ignoreCase = true) -> "Mobile"
                userAgent.contains("Tablet", ignoreCase = true) -> "Tablet"
                else -> "Desktop"
            }
        }
    }

    /**
     * 세션 정보가 유효한지 확인
     */
    fun isValid(): Boolean {
        return deviceInfo != null || ipAddress != null || userAgent != null
    }
}