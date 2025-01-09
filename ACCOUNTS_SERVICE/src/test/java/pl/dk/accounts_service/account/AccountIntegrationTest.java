package pl.dk.accounts_service.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.enums.CurrencyType;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;
import pl.dk.accounts_service.error.RestControllerHandler;
import pl.dk.accounts_service.httpClient.UserFeignClient;
import pl.dk.accounts_service.httpClient.dtos.UserDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dk.accounts_service.kafka.KafkaConstants.CREATE_TRANSFER_EVENT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
@Transactional
@EmbeddedKafka(topics = CREATE_TRANSFER_EVENT)
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class AccountIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private UserFeignClient userFeignClient;

    String userId;
    UserDto userDto;
    CreateAccountDto createAccountDto;

    @BeforeEach
    void setUp() {
        userId = "63d520d6-df76-4ed7-a8a6-2f597248cfb1";
        userDto = UserDto.builder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .phone("+48669964099")
                .email("dkcodepro@gmail.com")
                .build();
        createAccountDto = CreateAccountDto.builder()
                .accountType("CREDIT")
                .userId(userId)
                .build();
    }

    @Test
    @DisplayName("Typical scenario with Controller endpoints")
    void typicalScenarioWithControllerEndpoints() {
        // 1. User tries to create account with valid data: Expected status: 201 CREATED
        // Given
        Mockito.when(userFeignClient.getUserById(userId)).thenReturn(ResponseEntity.of(Optional.of(userDto)));

        // When
        HttpEntity<CreateAccountDto> createAccounttHttpEntity = new HttpEntity<>(createAccountDto);
        ResponseEntity<AccountDto> postResponse = testRestTemplate.exchange(
                "/accounts",
                HttpMethod.POST,
                createAccounttHttpEntity,
                AccountDto.class);

        // Then
        AccountDto accountDtoPOST = postResponse.getBody();
        assertAll(() -> {
            assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
            assertNotNull(accountDtoPOST);
            assertEquals(createAccountDto.accountType(), accountDtoPOST.accountType());
            assertEquals(createAccountDto.userId(), accountDtoPOST.userId());
            assertEquals(true, accountDtoPOST.active());
        });

        // 2. User tries to get account info by given id(account number): Expected status: 200 OK
        // Given
        String postAccountNumber = accountDtoPOST.accountNumber();

        // When
        ResponseEntity<AccountDto> getResponse = testRestTemplate.exchange(
                "/accounts/{accountNumber}",
                HttpMethod.GET,
                null,
                AccountDto.class,
                postAccountNumber);

        // Then
        AccountDto accountDtoGET = getResponse.getBody();
        assertAll(() -> {
            assertEquals(HttpStatus.OK, getResponse.getStatusCode());
            assertNotNull(accountDtoGET);
            assertEquals(accountDtoPOST.accountType(), accountDtoGET.accountType());
            assertEquals(accountDtoPOST.userId(), accountDtoGET.userId());
            assertEquals(accountDtoPOST.active(), accountDtoGET.active());
        });

        // 3. User tries to update bank account balance: Expected status: 200 OK
        // Given
        BigDecimal updateByValue = BigDecimal.valueOf(1);
        UpdateAccountBalanceDto updateAccountBalance = UpdateAccountBalanceDto.builder()
                .currencyType(CurrencyType.PLN.name())
                .updateByValue(updateByValue)
                .build();
        HttpEntity<UpdateAccountBalanceDto> updateAccounttHttpEntity = new HttpEntity<>(updateAccountBalance);

        // When
        ResponseEntity<AccountBalanceDto> patchResponse = testRestTemplate.exchange(
                "/accounts/{accountNumber}",
                HttpMethod.PATCH,
                updateAccounttHttpEntity,
                AccountBalanceDto.class,
                postAccountNumber);

        // Then
        AccountBalanceDto accountBalanceDto = patchResponse.getBody();

        assertAll((() -> {
            assertEquals(HttpStatus.OK, patchResponse.getStatusCode());
            assertNotNull(accountBalanceDto);
        }));

        // 4. User wants to delete account: Expected status: 204 NO_CONTENT
        // Given When
        ResponseEntity<AccountDto> deleteResponse = testRestTemplate.exchange(
                "/accounts/{accountNumber}",
                HttpMethod.DELETE,
                null,
                AccountDto.class,
                postAccountNumber);

        // Then
        AccountDto accountDtoDELETE = deleteResponse.getBody();

        assertAll(() -> {
            assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
            assertNull(accountDtoDELETE);
        });

        // 5. User wants to update account balance but account does not exist: Expected status: 404 NOT_FOUND
        // Given When
        ResponseEntity<AccountDto> patch404Response = testRestTemplate.exchange(
                "/accounts/{accountNumber}",
                HttpMethod.PATCH,
                updateAccounttHttpEntity,
                AccountDto.class,
                postAccountNumber);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.NOT_FOUND, patch404Response.getStatusCode());
        });

        // 6. User tries to get all his accounts. Expected status code: 200 OK
        // Given When
        ResponseEntity<List<AccountDto>> allAccountsResponse = testRestTemplate.exchange("/accounts/{userId}/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AccountDto>>() {
                },
                userId);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.OK, allAccountsResponse.getStatusCode());
            assertEquals(1, allAccountsResponse.getBody().size());
        });

        // 7. User tires to get balance for PLN currency. Expected status code: 200 OK
        // Given When
        ResponseEntity<AccountBalanceDto> getAccountBalancePLN = testRestTemplate.getForEntity(
                "/accounts/{accountNumber}/balance?currencyType={currencyType}",
                AccountBalanceDto.class,
                postAccountNumber, CurrencyType.PLN.name());

        // Then
        assertAll(
                () -> {
                    assertEquals(HttpStatus.OK, getAccountBalancePLN.getStatusCode());
                }
        );

    }

    @Test
    @DisplayName("It should return bad request when account creation fails due to invalid data")
    void itShouldReturnBadRequestWhenAccountCreationFailsDueToInvalidData() {
        // 1. User tries to create account with valid data: Expected status: 400 BAD_REQUEST
        // Given
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .accountType("Invalid account type")
                .build();

        // When
        ParameterizedTypeReference<List<RestControllerHandler.MethodArgumentNotValidExceptionWrapper>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<RestControllerHandler.MethodArgumentNotValidExceptionWrapper>> postResponse =
                testRestTemplate.exchange(
                        "/accounts",
                        HttpMethod.POST,
                        new HttpEntity<>(createAccountDto),
                        responseType
                );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, postResponse.getStatusCode());
        List<RestControllerHandler.MethodArgumentNotValidExceptionWrapper> body = postResponse.getBody();
        assertAll(() -> {
            assertNotNull(body);
            assertEquals(2, postResponse.getBody().size());
        });

    }
}