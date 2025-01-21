package pl.dk.exchange_service.httpclient.dtos.dtos;

import lombok.Builder;
import pl.dk.exchange_service.enums.CurrencyType;

import java.math.BigDecimal;

@Builder
public record AccountBalanceDto(
        String accountBalanceId,
        CurrencyType currencyType,
        BigDecimal balance
) {
}
