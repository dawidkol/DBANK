package pl.dk.cardservice.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class CardExpiryDateValidator implements ConstraintValidator<CardExpiryConstraint, Integer> {

    private final List<Integer> allowedYears = List.of(3, 4, 5);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate("Allowed values: %s".formatted(allowedYears));
        return allowedYears.stream()
                .anyMatch(year -> year.equals(value));
    }

    @Override
    public void initialize(CardExpiryConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
