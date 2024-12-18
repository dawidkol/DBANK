package pl.dk.accounts_service.error;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.apache.commons.codec.language.bm.Lang;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AccountTypeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountTypeEnum {

    String message() default "Invalid account type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
