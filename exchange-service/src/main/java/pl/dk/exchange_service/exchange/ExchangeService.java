package pl.dk.exchange_service.exchange;

import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;

import java.math.BigDecimal;

interface ExchangeService {

    ExchangeDto exchangeCurrencies(ExchangeDto exchange);

    CalculateResult calculateExchange(CurrencyType from, CurrencyType to, BigDecimal valueFrom);
}
