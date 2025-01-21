package pl.dk.exchange_service.exchange.dtos;

import lombok.Builder;
import pl.dk.exchange_service.enums.CurrencyType;

import java.math.BigDecimal;

@Builder
public record ExchangeDto(
        String accountNumber,
        CurrencyType currencyFrom,
        CurrencyType currencyTo,
        BigDecimal valueFrom
) {
}
