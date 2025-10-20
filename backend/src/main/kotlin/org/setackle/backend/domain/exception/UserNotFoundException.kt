package org.setackle.backend.domain.exception

import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode

class UserNotFoundException(userId: Long) : BusinessException(
    ErrorCode.USER_NOT_FOUND,
    details = mapOf("userId" to userId),
)
