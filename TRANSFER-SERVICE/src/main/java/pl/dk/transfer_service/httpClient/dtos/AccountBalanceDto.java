package pl.dk.transfer_service.httpClient.dtos;

import lombok.Builder;
import pl.dk.transfer_service.enums.CurrencyType;

import java.math.BigDecimal;

@Builder
public record AccountBalanceDto(
        String accountBalanceId,
        CurrencyType currencyType,
        BigDecimal balance
) {
}
