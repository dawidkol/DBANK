package pl.dk.user_service.user.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record SaveUserDto(

        @NotBlank
        @Size(min = 2, max = 1000)
        String firstName,
        @NotBlank
        @Size(min = 2, max = 1000)
        String lastName,
        @Email
        String email,
        @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{9}$",
                message = "Invalid phone number. Must be a valid 9-digit number")
        String phone,
        @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must have at least one digit, one uppercase letter, one special character, and be at least 8 characters long.")
        String password,
        @Past
        LocalDate dateOfBirth

) {

}
