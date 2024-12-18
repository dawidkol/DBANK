package pl.dk.accounts_service.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.error.RestControllerHandler;
import pl.dk.accounts_service.httpClient.UserFeignClient;
import pl.dk.accounts_service.httpClient.dtos.UserDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
@Transactional
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
                .balance(BigDecimal.valueOf(1000))
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
        ResponseEntity<AccountDto> postResponse = testRestTemplate.exchange("/accounts", HttpMethod.POST, createAccounttHttpEntity, AccountDto.class);

        // Then
        AccountDto accountDtoPOST = postResponse.getBody();
        assertAll(() -> {
            assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
            assertNotNull(accountDtoPOST);
            assertEquals(createAccountDto.accountType(), accountDtoPOST.accountType());
            assertEquals(createAccountDto.balance(), accountDtoPOST.balance());
            assertEquals(createAccountDto.userId(), accountDtoPOST.userId());
            assertEquals(true, accountDtoPOST.active());
        });

        // 2. User tries to get account info by given id(account number): Expected status: 200 OK
        // Given
        String postAccountNumber = accountDtoPOST.accountNumber();

        // When
        ResponseEntity<AccountDto> getResponse = testRestTemplate.exchange("/accounts/{accountNumber}", HttpMethod.GET, null, AccountDto.class, postAccountNumber);

        // Then
        AccountDto accountDtoGET = getResponse.getBody();
        assertAll(() -> {
            assertEquals(HttpStatus.OK, getResponse.getStatusCode());
            assertNotNull(accountDtoGET);
            assertEquals(accountDtoPOST.accountType(), accountDtoGET.accountType());
            assertEquals(accountDtoPOST.balance(), accountDtoGET.balance());
            assertEquals(accountDtoPOST.userId(), accountDtoGET.userId());
            assertEquals(accountDtoPOST.active(), accountDtoGET.active());
        });

        // 3. User tries to update bank account balance: Expected status: 200 OK
        // Given
        BigDecimal updateByValue = BigDecimal.valueOf(1);
        HttpEntity<BigDecimal> updateAccounttHttpEntity = new HttpEntity<>(updateByValue);

        // When
        ResponseEntity<AccountDto> patchResponse = testRestTemplate.exchange("/accounts/{accountNumber}", HttpMethod.PATCH, updateAccounttHttpEntity, AccountDto.class, postAccountNumber);

        // Then
        AccountDto accountDtoPATCH = patchResponse.getBody();

        assertAll((() -> {
            assertEquals(HttpStatus.OK, patchResponse.getStatusCode());
            assertNotNull(accountDtoPATCH);
            assertNotEquals(accountDtoGET.balance(), accountDtoPATCH.balance());
            assertEquals(accountDtoGET.accountType(), accountDtoPATCH.accountType());
            assertEquals(accountDtoGET.userId(), accountDtoPATCH.userId());
            assertEquals(accountDtoGET.active(), accountDtoPATCH.active());
        }));

        // 4. User wants to delete account: Expected status: 204 NO_CONTENT
        // Given When
        ResponseEntity<AccountDto> deleteResponse = testRestTemplate.exchange("/accounts/{accountNumber}", HttpMethod.DELETE, null, AccountDto.class, postAccountNumber);

        // Then
        AccountDto accountDtoDELETE = deleteResponse.getBody();

        assertAll(() -> {
            assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
            assertNull(accountDtoDELETE);
        });

        // 5. User wants to update account balance but account does not exist: Expected status: 404 NOT_FOUND
        // Given When
        ResponseEntity<AccountDto> patch404Response = testRestTemplate.exchange("/accounts/{accountNumber}", HttpMethod.PATCH, updateAccounttHttpEntity, AccountDto.class, postAccountNumber);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.NOT_FOUND, patch404Response.getStatusCode());
        });

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
            assertEquals(3, postResponse.getBody().size());
        });

    }
}