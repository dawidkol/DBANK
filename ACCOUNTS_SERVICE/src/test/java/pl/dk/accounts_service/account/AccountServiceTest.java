package pl.dk.accounts_service.account;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.constants.PagingAndSorting;
import pl.dk.accounts_service.exception.AccountBalanceException;
import pl.dk.accounts_service.exception.AccountInactiveException;
import pl.dk.accounts_service.exception.AccountNotExistsException;
import pl.dk.accounts_service.exception.UserNotFoundException;
import pl.dk.accounts_service.httpClient.UserFeignClient;
import pl.dk.accounts_service.httpClient.dtos.UserDto;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


class AccountServiceTest {

    @Mock
    private UserFeignClient userFeignClient;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    private AutoCloseable autoCloseable;
    private AccountService underTest;

    String userId;
    String accountNumber;
    BigDecimal balance;
    Boolean active;

    Account account;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new AccountServiceImpl(accountRepository, accountNumberGenerator, userFeignClient, applicationEventPublisher);

        userId = "63d520d6-df76-4ed7-a8a6-2f597248cfb1";
        accountNumber = "123456789012345678901234";
        balance = BigDecimal.valueOf(10000L);
        active = true;

        account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.CREDIT)
                .balance(balance)
                .userId(userId)
                .active(active)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should create Account Successfully")
    void itShouldCreateAccountSuccessfully() {
        // Given
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
                () -> assertThat(accountDto.userId()).isEqualTo(userId),
                () -> assertThat(accountDto.active()).isEqualTo(active)
        );
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user tries to create account with non existing userId")
    void itShouldThrowUserNotExistsExceptionWhenUserTriesToCreateAccountWithNonExistingUserId() {
        // Given
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

    @Test
    @DisplayName("It should find account by given id successfully")
    void itShouldFindAccountByGivenIdSuccessfully() {
        // Given
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));

        // When
        AccountDto result = underTest.getAccountById(accountNumber);

        //Then
        Mockito.verify(accountRepository, Mockito.times(1)).findById(accountNumber);
        assertAll(
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.accountNumber()).isEqualTo(accountNumber),
                () -> assertThat(result.balance()).isEqualTo(balance),
                () -> assertThat(result.accountType()).isEqualTo(account.getAccountType().toString()),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.active()).isEqualTo(active)
        );

    }

    @Test
    @DisplayName("It should throw AccountNotFoundException when account not exists")
    void itShouldThrowAccountNotFoundExceptionWhenAccountNotExists() {
        // Given
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.empty());

        // When
        assertThrows(AccountNotExistsException.class, () -> underTest.getAccountById(accountNumber));

        //Then
        Mockito.verify(accountRepository, Mockito.times(1)).findById(accountNumber);

    }

    @Test
    @DisplayName("It should set account active property as false successfully by given id")
    void itShouldSetAccountActivePropertyAsFalseSuccessfullyByGivenId() {
        // Given
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));

        // When
        underTest.deleteAccountById(accountNumber);

        // Then
        assertFalse(account.getActive());
        Mockito.verify(accountRepository, Mockito.times(1)).findById(accountNumber);
    }

    @Test
    @DisplayName("It throw AccountNotExistsException when user tries to delete non existing account ")
    void itShouldThrowAccountNotExistsExceptionWhenUserTriesToDeleteNonExistingAccount() {
        // Given
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.empty());

        // When Then
        assertThrows(AccountNotExistsException.class, () -> underTest.deleteAccountById(accountNumber));

        // Then
        Mockito.verify(accountRepository, Mockito.times(1)).findById(accountNumber);
    }

    @Test
    @DisplayName("It should update account balance successfully")
    void itShouldUpdateAccountBalanceSuccessfully() {
        // Given
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));
        BigDecimal updateByValue = BigDecimal.valueOf(1000);

        // When
        AccountDto result = underTest.updateAccountBalance(accountNumber, updateByValue);

        // Then
        assertThat(result.balance()).isEqualTo(balance.add(updateByValue));
    }

    @Test
    @DisplayName("It should throw AccountInactiveException when account is inactive")
    void itShouldThrowAccountInactiveExceptionWhenAccountIsInactive() {
        // Given
        account.setActive(false);
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));
        BigDecimal updateByValue = BigDecimal.valueOf(1000);

        // When Then
        assertThrows(AccountInactiveException.class,
                () -> underTest.updateAccountBalance(accountNumber, updateByValue));
        Mockito.verify(accountRepository, Mockito.times(1)).findById(accountNumber);
    }

    @Test
    @DisplayName("It should throw AccountBalanceException when amount is greater that current account balance")
    void itShouldThrowAccountBalanceExceptionWhenAmountIsGreaterThanCurrentAccountBalance() {
        // Given
        Mockito.when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));
        BigDecimal updateByValue = BigDecimal.valueOf(-1000000);

        // When
        assertThrows(AccountBalanceException.class,
                () -> underTest.updateAccountBalance(accountNumber, updateByValue));
        Mockito.verify(accountRepository, Mockito.times(1)).findById(accountNumber);
    }

    @Test
    @DisplayName("It should return all user accounts")
    void itShouldReturnAllUserAccounts() {
        // Given
        int page = Integer.parseInt(PagingAndSorting.PAGE_DEFAULT);
        int size = Integer.parseInt(PagingAndSorting.SIZE_DEFAULT);

        PageImpl<Account> accountPage = new PageImpl<>(List.of(account));
        Mockito.when(accountRepository.findAllByUserId(userId, PageRequest.of(page - 1, size))).thenReturn(accountPage);

        // When
        List<AccountDto> result = underTest.getAllUserAccounts(userId, page, size);

        // Then
        assertAll(() -> {
            assertEquals(1, result.size());
        });
    }
}
