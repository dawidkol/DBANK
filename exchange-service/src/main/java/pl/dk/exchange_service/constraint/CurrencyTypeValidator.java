package pl.dk.exchange_service.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exception.CurrencyNotFoundException;
import pl.dk.exchange_service.exception.RequestParameterException;

import java.util.Arrays;

public class CurrencyTypeValidator implements ConstraintValidator<CurrencyTypeConstraint, CurrencyType> {

    @Override
    public boolean isValid(CurrencyType value, ConstraintValidatorContext context) {
        String upperCase = value.name().toUpperCase();
        try {
            CurrencyType currencyType = CurrencyType.valueOf(upperCase);
            return true;
        } catch (Exception e) {
            CurrencyType[] supportedCurrencies = CurrencyType.values();
            throw new RequestParameterException("Currency [%s] not supported. Supported supportedCurrencies: [%s["
                    .formatted(upperCase, Arrays.toString(supportedCurrencies)));
        }
    }

    @Override
    public void initialize(CurrencyTypeConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
