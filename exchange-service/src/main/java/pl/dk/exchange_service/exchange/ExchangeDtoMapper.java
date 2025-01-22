package pl.dk.exchange_service.exchange;

import pl.dk.exchange_service.exchange.dtos.ExchangeResultDto;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;

import java.util.Collection;

class ExchangeDtoMapper {

    public static ExchangeResultDto map(Exchange exchange, Collection<AccountBalanceDto> balancesAfterExchange) {
        return ExchangeResultDto.builder()
                .exchangeId(exchange.getId())
                .accountNumber(exchange.getAccountNumber())
                .currencyFrom(exchange.getCurrencyFrom())
                .valueFrom(exchange.getValueFrom())
                .currencyTo(exchange.getCurrencyTo())
                .rate(exchange.getRate())
                .result(exchange.getResult())
                .balancesAfterExchange(balancesAfterExchange)
                .build();
    }
}
