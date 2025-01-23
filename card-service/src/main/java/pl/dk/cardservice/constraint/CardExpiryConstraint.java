package pl.dk.cardservice.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Documented
@Constraint(validatedBy = CardExpiryDateValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CardExpiryConstraint {

    String message() default "Invalid yearsValid value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
