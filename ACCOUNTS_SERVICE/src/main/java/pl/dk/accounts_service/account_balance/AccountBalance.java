package pl.dk.accounts_service.account_balance;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.BaseEntity;
import pl.dk.accounts_service.enums.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_balances")
@Getter
@Setter
@NoArgsConstructor
public class AccountBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Enumerated(value = EnumType.STRING)
    private CurrencyType currencyType;
    private BigDecimal balance;
    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "accountNumber", nullable = false)
    private Account account;

    @Builder

    public AccountBalance(LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt,
                          String updatedBy, String id, CurrencyType currencyType, BigDecimal balance, Account account) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.currencyType = currencyType;
        this.balance = balance;
        this.account = account;
    }
}
