package pl.dk.exchange_service.exchange.dtos;

import lombok.Builder;
import pl.dk.exchange_service.constraint.CurrencyTypeConstraint;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;

import java.math.BigDecimal;
import java.util.Collection;

@Builder
public record ExchangeResultDto(
        String exchangeId,
        String accountNumber,
        @CurrencyTypeConstraint
        CurrencyType currencyFrom,
        BigDecimal valueFrom,
        @CurrencyTypeConstraint
        CurrencyType currencyTo,
        BigDecimal rate,
        BigDecimal result,
        Collection<AccountBalanceDto> balancesAfterExchange
) {
}
