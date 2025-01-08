package pl.dk.accounts_service.account_transaction;

import jakarta.persistence.*;
import lombok.*;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account_balance.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts_transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private BigDecimal balanceBeforeTransaction;
    private BigDecimal balanceAfterTransaction;
    @Enumerated(value = EnumType.STRING)
    private CurrencyType currencyType;
    @ManyToOne
    @JoinColumn(name = "account_number")
    private Account account;

}
