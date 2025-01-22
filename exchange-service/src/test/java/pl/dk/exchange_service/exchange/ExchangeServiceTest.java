package pl.dk.exchange_service.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pl.dk.exchange_service.currency.Currency;
import pl.dk.exchange_service.currency.CurrencyRepository;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;
import pl.dk.exchange_service.exchange.dtos.ExchangeResultDto;
import pl.dk.exchange_service.httpclient.AccountServiceFeignClient;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;
import pl.dk.exchange_service.httpclient.dtos.dtos.UpdateAccountBalanceDto;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ExchangeServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private ExchangeRepository exchangeRepository;
    @Mock
    private AccountServiceFeignClient accountServiceFeignClient;

    private ExchangeService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new ExchangeServiceImpl(accountServiceFeignClient, currencyRepository, exchangeRepository);
    }

    @Test
    @DisplayName("It should convert from PLN to USD")
    void itShouldConvertFromPLNtoUSD() {
        // Given
        CurrencyType from = CurrencyType.PLN;
        CurrencyType to = CurrencyType.USD;
        BigDecimal valueFrom = BigDecimal.valueOf(10);

        Currency PLN = Currency.builder()
                .bid(BigDecimal.ONE)
                .ask(BigDecimal.ONE)
                .currencyType(from)
                .build();

        Currency USD = Currency.builder()
                .bid(BigDecimal.valueOf(4))
                .ask(BigDecimal.valueOf(5))
                .currencyType(to)
                .build();

        when(currencyRepository.findFirstByCurrencyType(from)).thenReturn(Optional.of(PLN));
        when(currencyRepository.findFirstByCurrencyType(to)).thenReturn(Optional.of(USD));

        // When
        CalculateResult result = underTest.calculateExchange(from, to, valueFrom);

        // Then
        assertEquals(valueFrom, result.amountToSubtract());
        assertEquals(0, result.amountToAdd().compareTo(BigDecimal.TWO));
    }

    @Test
    @DisplayName("It should convert from USD to PLN")
    void itShouldConvertFromUSDtoPLN() {
        // Given
        CurrencyType from = CurrencyType.USD;
        CurrencyType to = CurrencyType.PLN;
        BigDecimal valueFrom = BigDecimal.valueOf(10);

        Currency currencyPLN = Currency.builder()
                .bid(BigDecimal.ONE)
                .ask(BigDecimal.ONE)
                .currencyType(to)
                .build();

        Currency currencyUSD = Currency.builder()
                .bid(BigDecimal.valueOf(4))
                .ask(BigDecimal.valueOf(5))
                .currencyType(from)
                .build();

        when(currencyRepository.findFirstByCurrencyType(from))
                .thenReturn(Optional.of(currencyUSD));
        when(currencyRepository.findFirstByCurrencyType(to))
                .thenReturn(Optional.of(currencyPLN));


        // When
        CalculateResult result = underTest.calculateExchange(from, to, valueFrom);

        // Then
        assertEquals(valueFrom, result.amountToSubtract());
        assertEquals(valueFrom.multiply(currencyPLN.getAsk()), result.amountToAdd());
    }

    @Test
    @DisplayName("It should convert from USD to GBP")
    void itShouldConvertFromUSDtoGBP() {
        // Given
        CurrencyType from = CurrencyType.USD;
        CurrencyType to = CurrencyType.GBP;
        BigDecimal valueFrom = BigDecimal.valueOf(1);

        Currency currencyUSD = Currency.builder()
                .bid(BigDecimal.valueOf(4))
                .ask(BigDecimal.valueOf(5))
                .currencyType(from)
                .build();

        Currency currencyGBP = Currency.builder()
                .bid(BigDecimal.valueOf(8))
                .ask(BigDecimal.valueOf(10))
                .currencyType(to)
                .build();

        when(currencyRepository.findFirstByCurrencyType(from))
                .thenReturn(Optional.of(currencyUSD));
        when(currencyRepository.findFirstByCurrencyType(to))
                .thenReturn(Optional.of(currencyGBP));

        // When
        CalculateResult result = underTest.calculateExchange(from, to, valueFrom);

        // Then
        assertEquals(0, BigDecimal.valueOf(0.5).compareTo(result.amountToAdd()));
    }

    @Test
    @DisplayName("It should exchange PLN to USD successfully")
    void itShouldExchangePLNtoUSDSuccessfully() {
        // Given
        ExchangeDto exchangeDto = ExchangeDto.builder()
                .currencyFrom(CurrencyType.PLN)
                .currencyTo(CurrencyType.USD)
                .valueFrom(BigDecimal.valueOf(10))
                .accountNumber(UUID.randomUUID().toString())
                .build();

        Currency PLN = Currency.builder()
                .bid(BigDecimal.ONE)
                .ask(BigDecimal.ONE)
                .currencyType(exchangeDto.currencyFrom())
                .build();

        Currency USD = Currency.builder()
                .bid(BigDecimal.valueOf(4))
                .ask(BigDecimal.valueOf(5))
                .currencyType(exchangeDto.currencyTo())
                .build();

        Exchange exchange = Exchange.builder()
                .accountNumber(UUID.randomUUID().toString())
                .currencyFrom(CurrencyType.USD)
                .valueFrom(BigDecimal.valueOf(100))
                .currencyTo(CurrencyType.EUR)
                .rate(BigDecimal.valueOf(0.85))
                .result(BigDecimal.valueOf(85))
                .build();

        when(currencyRepository.findFirstByCurrencyType(exchangeDto.currencyFrom()))
                .thenReturn(Optional.of(PLN));
        when(currencyRepository.findFirstByCurrencyType(exchangeDto.currencyTo()))
                .thenReturn(Optional.of(USD));
        when(accountServiceFeignClient.updateBalance(anyString(),
                any(UpdateAccountBalanceDto.class)))
                .thenReturn(ResponseEntity.status(200).body(AccountBalanceDto.builder().build()))
                .thenReturn(ResponseEntity.status(200).body(AccountBalanceDto.builder().build()));
        when(exchangeRepository.save(any(Exchange.class))).thenReturn(exchange);

        // When
        ExchangeResultDto result = underTest.exchangeCurrencies(exchangeDto);

        // Then
        assertAll(() -> {
            verify(currencyRepository, times(2))
                    .findFirstByCurrencyType(any(CurrencyType.class));
            verify(accountServiceFeignClient, times(2))
                    .updateBalance(anyString(), any(UpdateAccountBalanceDto.class));
            verify(exchangeRepository, times(1)).save(any(Exchange.class));
        }, () -> {
            assertEquals(2, result.balancesAfterExchange().size());
        });
    }
}