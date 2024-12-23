package pl.dk.accounts_service.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account extends BaseEntity{

    @Id
    private String accountNumber;
    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountType accountType;
    @PositiveOrZero
    @NotNull
    private BigDecimal balance;
    @NotBlank
    private String userId;
    @NotNull
    private Boolean active;

    @Builder
    public Account(LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, String accountNumber, AccountType accountType, BigDecimal balance, String userId, Boolean active) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.userId = userId;
        this.active = active;
    }
}
