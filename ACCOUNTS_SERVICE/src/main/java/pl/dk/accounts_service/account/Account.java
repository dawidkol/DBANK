package pl.dk.accounts_service.account;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
class Account extends BaseEntity{

    @Id
    private String accountNumber;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private BigDecimal balance;
    private String userId;

    @Builder

    public Account(LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, String accountNumber, AccountType accountType, BigDecimal balance, String userId) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.userId = userId;
    }
}
