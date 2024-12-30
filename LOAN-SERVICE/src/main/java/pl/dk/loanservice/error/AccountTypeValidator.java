package pl.dk.loanservice.error;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.dk.loanservice.loan.AccountType;

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
