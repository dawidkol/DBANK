package pl.dk.accounts_service.account_balance.dtos;

import lombok.Builder;
import pl.dk.accounts_service.enums.CurrencyType;

import java.math.BigDecimal;

@Builder
public record AccountBalanceDto(
        String accountBalanceId,
        CurrencyType currencyType,
        BigDecimal balance
) {
}
