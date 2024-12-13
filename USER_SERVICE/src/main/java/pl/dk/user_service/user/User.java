package pl.dk.user_service.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phone;
    private String password;
    private LocalDate dateOfBirth;

    @Builder
    public User(String id, LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, String firstName, String lastName, String email, String phone, String password, LocalDate dateOfBirth) {
        super(id, createdAt, createdBy, updatedAt, updatedBy);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }

}
