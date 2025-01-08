package pl.dk.accounts_service.account_balance.dtos;

import lombok.Builder;
import pl.dk.accounts_service.account_balance.CurrencyType;

import java.math.BigDecimal;

@Builder
public record AccountBalanceDto(
        CurrencyType currencyType,
        BigDecimal balance
) {
}
