package pl.dk.accounts_service.account_transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.AccountRepository;
import pl.dk.accounts_service.account.AccountType;
import pl.dk.accounts_service.account.dtos.AccountEventPublisher;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionCalculationDto;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionDto;
import pl.dk.accounts_service.exception.AccountNotExistsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AccountTransactionServiceTest {

    @Mock
    private AccountTransactionRepository accountTransactionRepository;
    @Mock
    private AccountRepository accountRepository;
    private AccountTransactionService underTest;

    AutoCloseable autoCloseable;

    AccountTransaction transaction;
    String transactionId;
    LocalDateTime transactionDate;
    BigDecimal amount;
    BigDecimal balanceBeforeTransaction;
    BigDecimal balanceAfterTransaction;
    Account account;
    String userId;
    BigDecimal balance;

    List<AccountTransactionCalculationDto> accountTransactionList;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new AccountTransactionServiceImpl(accountTransactionRepository, accountRepository);
        transactionId = UUID.randomUUID().toString();
        transactionDate = LocalDateTime.now();
        amount = new BigDecimal("500.00");
        balanceBeforeTransaction = new BigDecimal("1000.00");
        balanceAfterTransaction = new BigDecimal("1500.00");
        String userId = UUID.randomUUID().toString();
        balance = BigDecimal.valueOf(10000);

        account = Account.builder()
                .accountNumber("6311399679235673906780514")
                .accountType(AccountType.CREDIT)
                .balance(balance)
                .active(true)
                .userId(userId)
                .build();

        transaction = AccountTransaction.builder()
                .id(transactionId)
                .transactionDate(transactionDate)
                .amount(amount)
                .balanceBeforeTransaction(balanceBeforeTransaction)
                .balanceAfterTransaction(balanceAfterTransaction)
                .account(account)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should save transaction successfully")
    void itShouldSaveTransactionSuccessfully() {
        // Given
        Mockito.when(accountTransactionRepository.save(Mockito.any(AccountTransaction.class)))
                .thenReturn(transaction);

        // When
        AccountTransactionDto result = underTest.saveTransaction(new AccountTransaction());

        // Then
        assertAll(() -> {
            assertEquals(transactionId, result.id());
            assertEquals(transactionDate, result.transactionDate());
            assertEquals(amount, result.amount());
            assertEquals(balanceBeforeTransaction, result.balanceBeforeTransaction());
            assertEquals(balanceAfterTransaction, result.balanceAfterTransaction());
            assertEquals(account.getAccountNumber(), result.accountNumber());
        });
    }

    @Test
    @DisplayName("It should calculate average balance for declared months")
    void itShouldCalculateAverageBalanceForDeclaredMonths() {
        // Given
        accountTransactionList = new LinkedList<>();
        for (int i = 1; i <= 10; i++) {
            AccountTransactionCalculationDto transaction = AccountTransactionCalculationDto.builder()
                    .id(UUID.randomUUID().toString())
                    .transactionDate(LocalDateTime.now().minusMonths(1).minusDays(i))
                    .amount(BigDecimal.valueOf(i))
                    .build();
            accountTransactionList.add(transaction);
        }
        Mockito.when(accountTransactionRepository.findAllByAccount_UserIdAndTransactionDateIsBetween(
                        any(),
                        any(),
                        any()))
                .thenReturn(accountTransactionList);

        // When
        BigDecimal averageBalanceFromDeclaredMonths = underTest.getAverageBalanceFromDeclaredMonths(userId, 12);

        // Then
        assertAll(() -> {
                    Mockito.verify(accountTransactionRepository, Mockito.times(1))
                            .findAllByAccount_UserIdAndTransactionDateIsBetween(
                                    any(),
                                    any(),
                                    any());
                    assertEquals(BigDecimal.valueOf(5.5), averageBalanceFromDeclaredMonths);
                }
        );
    }

    @Test
    @DisplayName("It should save transaction successfully on transaction event")
    void itShouldSaveTransactionSuccessfullyOnTransactionEvent() {
        // Given
        Mockito.when(accountRepository.findById(any()))
                .thenReturn(Optional.of(account));
        Mockito.when(accountTransactionRepository.save(any(AccountTransaction.class)))
                .thenReturn(transaction);
        AccountEventPublisher accountEventPublisher = AccountEventPublisher.builder()
                .updatedByValue(BigDecimal.valueOf(100))
                .accountId(account.getAccountNumber())
                .build();

        // When
        underTest.onTransactionEvent(accountEventPublisher);

        // Then
        assertAll(() -> {
            Mockito.verify(accountRepository, Mockito.times(1)).findById(any());
            Mockito.verify(accountTransactionRepository, Mockito.times(1)).save(any(AccountTransaction.class));
        });
    }

    @Test
    @DisplayName("It should throw AccountNotFoundException")
    void itShouldSaveThrowAccountNotExistsException() {
        // Given
        Mockito.when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());
        Mockito.when(accountTransactionRepository.save(any(AccountTransaction.class)))
                .thenReturn(transaction);
        AccountEventPublisher accountEventPublisher = AccountEventPublisher.builder()
                .updatedByValue(BigDecimal.valueOf(100))
                .accountId(account.getAccountNumber())
                .build();

        // When Then
        assertAll(() -> {
            assertThrows(AccountNotExistsException.class, () -> underTest.onTransactionEvent(accountEventPublisher));
            Mockito.verify(accountRepository, Mockito.times(1)).findById(any());
        });
    }
}