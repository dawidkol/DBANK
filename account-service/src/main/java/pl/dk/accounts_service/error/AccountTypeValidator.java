package pl.dk.accounts_service.error;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.dk.accounts_service.enums.AccountType;

class AccountTypeValidator implements ConstraintValidator<AccountTypeEnum, String> {

    @Override
    public void initialize(AccountTypeEnum constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        try {
            AccountType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            result = false;
        }
        return result;
    }
}
