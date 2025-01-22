package pl.dk.exchange_service.exchange;

import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;
import pl.dk.exchange_service.exchange.dtos.ExchangeResultDto;

import java.math.BigDecimal;
import java.util.List;

interface ExchangeService {

    ExchangeResultDto exchangeCurrencies(ExchangeDto exchange);

    CalculateResult calculateExchange(CurrencyType from, CurrencyType to, BigDecimal valueFrom);

    List<ExchangeResultDto> getAllAccountExchanges(String accountNumber, int page, int size);
}
