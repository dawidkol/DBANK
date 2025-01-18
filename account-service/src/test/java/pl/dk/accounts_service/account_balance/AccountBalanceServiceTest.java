package pl.dk.accounts_service.account_balance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.AccountRepository;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;
import pl.dk.accounts_service.enums.CurrencyType;
import pl.dk.accounts_service.exception.AccountBalanceException;
import pl.dk.accounts_service.exception.AccountBalanceNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountBalanceServiceTest {

    @Mock
    private AccountBalanceRepository accountBalanceRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    AutoCloseable autoCloseable;

    private AccountBalanceService underTest;

    private Account account;
    private String accountNumber;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new AccountBalanceServiceImpl(
                accountBalanceRepository,
                accountRepository,
                applicationEventPublisher);

        accountNumber = UUID.randomUUID().toString();
        account = Account.builder()
                .accountNumber(accountNumber)
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should update account balance successfully for every available currency when account balance already exists")
    void itShouldUpdateAccountBalanceSuccessfully(CurrencyType currencyType) {
        // Given
        UpdateAccountBalanceDto updateAccountBalanceDto = UpdateAccountBalanceDto.builder()
                .currencyType(currencyType.name())
                .updateByValue(valueOf(100))
                .build();

        when(accountRepository.findById(accountNumber))
                .thenReturn(Optional.of(Account.builder()
                        .accountNumber(accountNumber)
                        .active(true)
                        .build()));

        AccountBalance accountBalance = AccountBalance.builder()
                .balance(TEN)
                .currencyType(currencyType)
                .build();

        when(accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType))
                .thenReturn(Optional.of(accountBalance));

        // When
        AccountBalanceDto result = underTest.updateAccountBalance(accountNumber, updateAccountBalanceDto);

        // Then
        assertAll(() -> {
            verify(accountRepository, times(1)).findById(accountNumber);
            verify(accountBalanceRepository, times(1))
                    .findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType);
        }, () -> {
            assertEquals(0, accountBalance.getBalance().compareTo(result.balance()));
            assertEquals(currencyType, result.currencyType());
        });
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should update and create account balance successfully for every available currency when account balance not exists")
    void itShouldUpdateAccountBalanceSuccessfullyWhenAccountBalanceNotExists(CurrencyType currencyType) {
        // Given
        UpdateAccountBalanceDto updateAccountBalanceDto = UpdateAccountBalanceDto.builder()
                .currencyType(currencyType.name())
                .updateByValue(valueOf(100))
                .build();

        when(accountRepository.findById(accountNumber))
                .thenReturn(Optional.of(account));

        when(accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType))
                .thenReturn(Optional.empty());

        when(accountBalanceRepository.save(any())).thenReturn(AccountBalance.builder()
                .account(account)
                .currencyType(currencyType)
                .balance(updateAccountBalanceDto.updateByValue())
                .build());

        // When
        AccountBalanceDto result = underTest.updateAccountBalance(accountNumber, updateAccountBalanceDto);

        // Then
        assertAll(() -> {
            verify(accountRepository, times(1)).findById(accountNumber);
            verify(accountBalanceRepository, times(1))
                    .findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType);
            verify(accountBalanceRepository, times(1)).save(any());
        }, () -> {
            assertEquals(-1, ZERO.compareTo(result.balance()));
            assertEquals(currencyType, result.currencyType());
        });
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should throw AccountBalanceException when result would go below zero")
    void itShouldThrowAccountBalanceExceptionWhenResultWouldGoBelowZero(CurrencyType currencyType) {
        // Given
        UpdateAccountBalanceDto updateAccountBalanceDto = UpdateAccountBalanceDto.builder()
                .currencyType(currencyType.name())
                .updateByValue(valueOf(-100))
                .build();

        when(accountRepository.findById(accountNumber))
                .thenReturn(Optional.of(account));

        when(accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
            assertThrows(AccountBalanceException.class, () -> underTest.updateAccountBalance(accountNumber, updateAccountBalanceDto));
            verify(accountRepository, times(1)).findById(accountNumber);
            verify(accountBalanceRepository, times(1))
                    .findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType);
        });
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should throw AccountBalanceException when result would go below zero with existing account balance")
    void itShouldThrowAccountBalanceExceptionWhenResultWouldGoBelowZeroWithExistingAccountBalance(CurrencyType currencyType) {
        // Given
        UpdateAccountBalanceDto updateAccountBalanceDto = UpdateAccountBalanceDto.builder()
                .currencyType(currencyType.name())
                .updateByValue(valueOf(-100))
                .build();

        when(accountRepository.findById(accountNumber))
                .thenReturn(Optional.of(Account.builder()
                        .accountNumber(accountNumber)
                        .active(true)
                        .build()));

        AccountBalance accountBalance = AccountBalance.builder()
                .balance(TEN)
                .currencyType(currencyType)
                .account(account)
                .build();

        when(accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType))
                .thenReturn(Optional.of(accountBalance));

        // When Then
        assertAll(() -> {
            assertThrows(AccountBalanceException.class, () -> underTest.updateAccountBalance(accountNumber, updateAccountBalanceDto));
            verify(accountRepository, times(1)).findById(accountNumber);
            verify(accountBalanceRepository, times(1))
                    .findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType);
        });
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should get account balance by given account number and currency type")
    void itShouldGetAccountBalanceByGivenAccountNumberAndCurrencyType(CurrencyType currencyType) {
        // Given
        AccountBalance accountBalance = AccountBalance.builder()
                .id(UUID.randomUUID().toString())
                .balance(TEN)
                .currencyType(currencyType)
                .account(account)
                .build();

        when(accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType))
                .thenReturn(Optional.of(accountBalance));

        // When
        AccountBalanceDto result = underTest.getAccountBalanceByAccountNumberAndCurrencyType(accountNumber, currencyType.name());

        // Then
        assertAll(() -> {
                    verify(accountBalanceRepository, times(1)).findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType);

                }, () -> {
                    assertNotNull(result.accountBalanceId());
                    assertEquals(accountBalance.getBalance(), result.balance());
                    assertEquals(accountBalance.getCurrencyType(), result.currencyType());
                }
        );
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should throw AccountBalanceNotFoundException when user tries to get all")
    void itShouldThrowAccountBalanceNotFoundException(CurrencyType currencyType) {
        // Given
        when(accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
            assertThrows(AccountBalanceNotFoundException.class, () -> underTest.getAccountBalanceByAccountNumberAndCurrencyType(accountNumber, currencyType.name()));
                    verify(accountBalanceRepository, times(1)).findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType);

                }
        );
    }
}