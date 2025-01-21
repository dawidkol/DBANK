package pl.dk.exchange_service.exchange;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.exchange_service.currency.Currency;
import pl.dk.exchange_service.currency.CurrencyRepository;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exception.ExchangeRequestException;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;
import pl.dk.exchange_service.httpclient.AccountServiceFeignClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static pl.dk.exchange_service.enums.CurrencyType.PLN;

@Service
@RequiredArgsConstructor
class ExchangeServiceImpl implements ExchangeService {

    private final AccountServiceFeignClient accountServiceFeignClient;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional(readOnly = true)
    public ExchangeDto exchangeCurrencies(ExchangeDto exchange) {
        return null;
    }

    @Override
    public CalculateResult calculateExchange(CurrencyType from, CurrencyType to, BigDecimal valueFrom) {
        Currency currencyFrom = getCurrencyOrThrow(from);
        Currency currencyTo = getCurrencyOrThrow(to);
        return performCurrencyConversion(valueFrom, currencyFrom, currencyTo);
    }

    private Currency getCurrencyOrThrow(CurrencyType currencyType) {
        return currencyRepository.findFirstByCurrencyType(currencyType)
                .orElseThrow(() ->
                        new ExchangeRequestException("Unsupported currency. Currency with code %s is not supported"
                                .formatted(currencyType))
                );
    }

    private CalculateResult performCurrencyConversion(BigDecimal valueFrom, Currency currencyFrom, Currency currencyTo) {
        CalculateResult result;
        if (currencyFrom.getCurrencyType().equals(PLN) && !currencyTo.getCurrencyType().equals(PLN)) {
            BigDecimal add = valueFrom.divide(currencyTo.getAsk(), 2, RoundingMode.HALF_UP);
            result = new CalculateResult(valueFrom, add);
        } else if (!currencyFrom.getCurrencyType().equals(PLN) && currencyTo.getCurrencyType().equals(PLN)) {
            BigDecimal add = valueFrom.multiply(currencyTo.getAsk());
            result = new CalculateResult(valueFrom, add);
        } else {
            BigDecimal valueFromPLN = valueFrom.multiply(currencyFrom.getBid());
            BigDecimal add = valueFromPLN.divide(currencyTo.getAsk(), 2, RoundingMode.HALF_UP);
            result = new CalculateResult(valueFromPLN, add);
        }
        return result;
    }


}
