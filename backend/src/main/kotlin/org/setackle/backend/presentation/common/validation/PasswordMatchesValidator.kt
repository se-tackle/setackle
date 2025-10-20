package org.setackle.backend.presentation.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.setackle.backend.presentation.user.dto.RegisterRequest

/**
 * PasswordMatches 어노테이션의 검증 로직 구현
 * RegisterRequest의 password와 confirmPassword 필드가 일치하는지 검증
 */
class PasswordMatchesValidator : ConstraintValidator<PasswordMatches, RegisterRequest> {

    override fun isValid(value: RegisterRequest?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }

        return value.password == value.confirmPassword
    }
}
