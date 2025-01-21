package pl.dk.exchange_service.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.dk.exchange_service.currency.Currency;
import pl.dk.exchange_service.currency.CurrencyRepository;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.httpclient.AccountServiceFeignClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ExchangeServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private AccountServiceFeignClient accountServiceFeignClient;
    private ExchangeService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new ExchangeServiceImpl(accountServiceFeignClient, currencyRepository);
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
        assertEquals(0, BigDecimal.valueOf(0.4).compareTo(result.amountToAdd()));
    }
}