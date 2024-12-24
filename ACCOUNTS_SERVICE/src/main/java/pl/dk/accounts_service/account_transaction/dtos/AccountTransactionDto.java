package pl.dk.accounts_service.account_transaction.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AccountTransactionDto(
        String id,
        LocalDateTime transactionDate,
        BigDecimal amount,
        BigDecimal balanceBeforeTransaction,
        BigDecimal balanceAfterTransaction,
        String accountNumber
) {
}
