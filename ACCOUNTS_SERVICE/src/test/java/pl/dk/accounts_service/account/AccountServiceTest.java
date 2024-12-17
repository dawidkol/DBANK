package pl.dk.accounts_service.account;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.exception.UserNotFoundException;
import pl.dk.accounts_service.httpClient.UserFeignClient;
import pl.dk.accounts_service.httpClient.dtos.UserDto;


import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;


class AccountServiceTest {

    @Mock
    private UserFeignClient userFeignClient;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    private AutoCloseable autoCloseable;
    private AccountService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new AccountServiceImpl(accountRepository, accountNumberGenerator, userFeignClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should create Account Successfully")
    void itShouldCreateAccountSuccessfully() {
        // Given
        String userId = "63d520d6-df76-4ed7-a8a6-2f597248cfb1";
        BigDecimal balance = BigDecimal.valueOf(10000L);
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .accountType("CREDIT")
                .balance(balance)
                .userId(userId)
                .build();

        UserDto userDto = UserDto.builder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .phone("+48669964099")
                .email("dkcodepro@gmail.com")
                .build();

        String accountNumber = "123456789012345678901234";
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.CREDIT)
                .balance(balance)
                .userId(userId)
                .build();

        ResponseEntity<UserDto> mockResponse = ResponseEntity.ok(userDto);
        Mockito.when(userFeignClient.getUserById(createAccountDto.userId())).thenReturn(mockResponse);
        Mockito.when(accountNumberGenerator.generateAccountNumber()).thenReturn(accountNumber);
        Mockito.when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        AccountDto accountDto = underTest.createAccount(createAccountDto);

        // Then
        Mockito.verify(userFeignClient, Mockito.times(1)).getUserById(userId);
        Mockito.verify(accountNumberGenerator, Mockito.atLeast(1)).generateAccountNumber();
        Mockito.verify(accountRepository, Mockito.times(1)).save(any(Account.class));

        assertAll(
                () -> assertThat(accountDto.accountNumber()).isEqualTo(accountNumber),
                () -> assertThat(accountDto.accountType()).isEqualTo(AccountType.CREDIT.toString()),
                () -> assertThat(accountDto.accountNumber()).isEqualTo(accountNumber),
                () -> assertThat(accountDto.balance()).isEqualTo(balance),
                () -> assertThat(accountDto.userId()).isEqualTo(userId)
        );
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user tries to create account with non existing userId")
    void itShouldThrowUserNotExistsExceptionWhenUserTriesToCreateAccountWithNonExistingUserId() {
        // Given
        String userId = "63d520d6-df76-4ed7-a8a6-2f597248cfb1";
        BigDecimal balance = BigDecimal.valueOf(10000L);
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .accountType("CREDIT")
                .balance(balance)
                .userId(userId)
                .build();
        Mockito.when(userFeignClient.getUserById(any(String.class))).thenReturn(ResponseEntity.notFound().build());

        // When
        assertThrows(UserNotFoundException.class, () -> underTest.createAccount(createAccountDto));

        // Then
        Mockito.verify(userFeignClient, Mockito.times(1)).getUserById(userId);
    }

}