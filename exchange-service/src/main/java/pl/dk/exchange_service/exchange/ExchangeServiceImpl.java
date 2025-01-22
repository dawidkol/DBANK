package pl.dk.exchange_service.exchange;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.exchange_service.currency.Currency;
import pl.dk.exchange_service.currency.CurrencyRepository;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exception.ExchangeRequestException;
import pl.dk.exchange_service.exception.ExchangeServiceUnavailable;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;
import pl.dk.exchange_service.exchange.dtos.ExchangeResultDto;
import pl.dk.exchange_service.httpclient.AccountServiceFeignClient;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;
import pl.dk.exchange_service.httpclient.dtos.dtos.UpdateAccountBalanceDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static pl.dk.exchange_service.enums.CurrencyType.PLN;

@Service
@RequiredArgsConstructor
class ExchangeServiceImpl implements ExchangeService {

    private final AccountServiceFeignClient accountServiceFeignClient;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRepository exchangeRepository;

    @Override
    @Transactional
    public ExchangeResultDto exchangeCurrencies(ExchangeDto exchange) {
        CurrencyType currencyFrom = exchange.currencyFrom();
        CurrencyType currencyTo = exchange.currencyTo();
        BigDecimal valueFrom = exchange.valueFrom();
        CalculateResult result = calculateExchange(currencyFrom, currencyTo, valueFrom);

        String accountNumber = exchange.accountNumber();
        UpdateAccountBalanceDto subtract = UpdateAccountBalanceDto.builder()
                .updateByValue(result.amountToSubtract().negate())
                .currencyType(exchange.currencyFrom().name())
                .build();
        ResponseEntity<AccountBalanceDto> accountSubtract
                = accountServiceFeignClient.updateBalance(accountNumber, subtract);

        UpdateAccountBalanceDto add = UpdateAccountBalanceDto.builder()
                .updateByValue(result.amountToAdd())
                .currencyType(exchange.currencyTo().name())
                .build();
        ResponseEntity<AccountBalanceDto> accountAdd
                = accountServiceFeignClient.updateBalance(accountNumber, add);

        if (accountAdd.getStatusCode().isSameCodeAs(OK) &&
            accountSubtract.getStatusCode().isSameCodeAs(OK)) {
            Exchange exchangeToSave = buildExchangeObject(accountNumber, subtract, add, result);
            Exchange savedExchange = exchangeRepository.save(exchangeToSave);
            List<AccountBalanceDto> balancesAfterExchange = List.of(accountSubtract.getBody(), accountAdd.getBody());
            return ExchangeDtoMapper.map(savedExchange, balancesAfterExchange);
        }
        throw new ExchangeServiceUnavailable("Internal server error, try again later");
    }

    private Exchange buildExchangeObject(String accountNumber,
                                         UpdateAccountBalanceDto subtract,
                                         UpdateAccountBalanceDto add,
                                         CalculateResult result) {
        return Exchange.builder()
                .accountNumber(accountNumber)
                .currencyFrom(CurrencyType.valueOf(subtract.currencyType()))
                .valueFrom(subtract.updateByValue().abs())
                .currencyTo(CurrencyType.valueOf(add.currencyType()))
                .rate(result.rate())
                .result(add.updateByValue())
                .build();
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
            result = new CalculateResult(valueFrom, add, currencyTo.getAsk());
        } else if (!currencyFrom.getCurrencyType().equals(PLN) && currencyTo.getCurrencyType().equals(PLN)) {
            BigDecimal add = valueFrom.multiply(currencyTo.getAsk());
            result = new CalculateResult(valueFrom, add, currencyTo.getAsk());
        } else {
            BigDecimal rate = currencyFrom.getAsk().divide(currencyTo.getAsk(), 2, RoundingMode.HALF_UP);
            BigDecimal add = valueFrom.multiply(rate);
            result = new CalculateResult(valueFrom, add, rate);
        }
        return result;
    }

}
