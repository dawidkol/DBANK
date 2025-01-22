package pl.dk.exchange_service.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrencyTypeConstraint {

    String message() default "Invalid currencyType";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
