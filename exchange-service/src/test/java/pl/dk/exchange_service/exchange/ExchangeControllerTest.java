package pl.dk.exchange_service.exchange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;
import pl.dk.exchange_service.exchange.dtos.ExchangeResultDto;
import pl.dk.exchange_service.httpclient.AccountServiceFeignClient;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;
import pl.dk.exchange_service.httpclient.dtos.dtos.UpdateAccountBalanceDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExchangeControllerTest {

    @MockitoBean
    private AccountServiceFeignClient accountServiceFeignClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @DisplayName("It should perform exchange operations successfully")
    void itShouldPerformExchangeOperationsSuccessfully() {
        // 1. User wants to calculate Exchange before making real exchange. Expected status code: 200 OK
        // Given
        String uriString = ServletUriComponentsBuilder.fromUriString("/exchanges")
                .queryParam("from", CurrencyType.PLN)
                .queryParam("to", CurrencyType.USD)
                .queryParam("valueFrom", BigDecimal.valueOf(100))
                .toUriString();
        // When
        ResponseEntity<CalculateResult> forEntity = testRestTemplate.getForEntity(uriString, CalculateResult.class);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.OK, forEntity.getStatusCode());
            assertNotNull(forEntity.getBody());
        });

        // 2. User wants to exchange currencies: PLN to USD. Expected status code: 201 CREATED
        // Given
        Mockito.when(accountServiceFeignClient.updateBalance(anyString(),
                        any(UpdateAccountBalanceDto.class))
                ).thenReturn(ResponseEntity.ok(AccountBalanceDto.builder()
                        .accountBalanceId(UUID.randomUUID().toString())
                        .currencyType(CurrencyType.PLN)
                        .balance(BigDecimal.valueOf(900)).build()))
                .thenReturn(ResponseEntity.ok(AccountBalanceDto.builder()
                        .accountBalanceId(UUID.randomUUID().toString())
                        .currencyType(CurrencyType.USD)
                        .balance(BigDecimal.valueOf(10)).build()));

        ExchangeDto exchangeDto = ExchangeDto.builder()
                .accountNumber(UUID.randomUUID().toString())
                .currencyFrom(CurrencyType.PLN)
                .valueFrom(BigDecimal.valueOf(100))
                .currencyTo(CurrencyType.USD)
                .build();

        // When
        ResponseEntity<ExchangeResultDto> exchangeResultDtoResponseEntity
                = testRestTemplate.postForEntity("/exchanges", exchangeDto, ExchangeResultDto.class);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.CREATED, exchangeResultDtoResponseEntity.getStatusCode());
            assertNotNull(exchangeResultDtoResponseEntity.getBody());
        });

        // 3. User wants to get all his exchanges. Expected status code: 200 OK
        // Given // When
        ResponseEntity<List<ExchangeResultDto>> exchange = testRestTemplate.exchange(
                "/exchanges/{accountNumber}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ExchangeResultDto>>() {
                },
                exchangeDto.accountNumber());

        assertAll(() -> {
            assertEquals(HttpStatus.OK, exchange.getStatusCode());
            assertNotNull(exchange.getBody());
            assertEquals(1, exchange.getBody().size());
        });
    }
}