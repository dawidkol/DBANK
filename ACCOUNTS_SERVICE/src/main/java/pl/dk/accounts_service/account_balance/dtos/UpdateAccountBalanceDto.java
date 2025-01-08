package pl.dk.accounts_service.account_balance.dtos;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UpdateAccountBalanceDto(
        String currencyType,
        BigDecimal amount
) {
}
