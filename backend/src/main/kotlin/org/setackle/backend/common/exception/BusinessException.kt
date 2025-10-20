package org.setackle.backend.common.exception

open class BusinessException(
    val errorCode: ErrorCode,
    val details: Map<String, Any?> = emptyMap(),
) : RuntimeException(errorCode.message)
