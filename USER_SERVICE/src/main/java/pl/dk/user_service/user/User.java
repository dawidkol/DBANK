package pl.dk.user_service.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
class User extends BaseEntity {

    @NotBlank
    @Size(min = 2, max = 1000)
    private String firstName;
    @NotBlank
    @Size(min = 2, max = 1000)
    private String lastName;
    @Column(unique = true)
    @Email
    private String email;
    @Column(unique = true)
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{9}$",
            message = "Invalid phone number. Must be a valid 9-digit number")
    private String phone;
    @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must have at least one digit, one uppercase letter, one special character, and be at least 8 characters long.")
    private String password;
    @Past
    private LocalDate dateOfBirth;
    @NotNull
    private Boolean active;

    @Builder
    public User(String id, LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, String firstName, String lastName, String email, String phone, String password, LocalDate dateOfBirth, Boolean active) {
        super(id, createdAt, createdBy, updatedAt, updatedBy);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.active = active;
    }
}
