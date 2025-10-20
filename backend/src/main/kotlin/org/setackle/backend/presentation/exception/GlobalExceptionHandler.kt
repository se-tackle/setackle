package org.setackle.backend.presentation.exception

import org.setackle.backend.presentation.common.ApiResponse
import org.setackle.backend.presentation.common.ErrorResponse
import org.setackle.backend.common.exception.BusinessException
import org.setackle.backend.common.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn(
            "Business Exception: " +
                    "ex=${ex.message}, " +
                    "code=${ex.errorCode.code}, " +
                    "message=${ex.errorCode.message}, " +
                    "details=${ex.details}, " +
                    "uri=${request.getDescription(false)}"
        )

        val errorResponse = ErrorResponse(
            code = ex.errorCode.code,
            message = ex.errorCode.message,
            details = ex.details
        )

        return ResponseEntity(ApiResponse.Companion.error(errorResponse), ex.errorCode.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = if (error is FieldError) {
                error.field
            } else {
                error.objectName
            }
            fieldName to (error.defaultMessage ?: "유효하지 않은 값입니다")
        }

        logger.warn("Validation failed: $errors, uri=${request.getDescription(false)}")

        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val errorResponse = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            details = errors
        )

        return ResponseEntity(ApiResponse.Companion.error(errorResponse), errorCode.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Uncaught Exception: ${ex.message}, uri=${request.getDescription(false)}", ex)

        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse(code = errorCode.code, message = errorCode.message)

        return ResponseEntity(ApiResponse.Companion.error(errorResponse), errorCode.status)
    }
}