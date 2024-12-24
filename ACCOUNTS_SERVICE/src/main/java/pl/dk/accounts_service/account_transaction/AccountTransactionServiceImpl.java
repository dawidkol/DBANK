package pl.dk.accounts_service.account_transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.AccountRepository;
import pl.dk.accounts_service.account.dtos.AccountEventPublisher;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionCalculationDto;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionDto;
import pl.dk.accounts_service.exception.AccountNotExistsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class AccountTransactionServiceImpl implements AccountTransactionService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountTransactionDto saveTransaction(AccountTransaction transaction) {
        AccountTransaction savedTransaction = accountTransactionRepository.save(transaction);
        return AccountTransactionDtoMapper.map(savedTransaction);
    }

    @Override
    @ApplicationModuleListener
    public void onTransactionEvent(AccountEventPublisher event) {
        log.info("Starting saving account transaction for accountId: {}", event.accountId());
        Account account = accountRepository.findById(event.accountId())
                .orElseThrow(() ->
                        new AccountNotExistsException("Account with id: %s not exists".formatted(event.accountId())));
        BigDecimal balance = account.getBalance();
        BigDecimal amount = event.updatedByValue();
        AccountTransaction accountTransactionToSave = buildAccountTransactionObject(account, amount, balance);
        AccountTransaction savedTransaction = accountTransactionRepository.save(accountTransactionToSave);
        log.info("Account transaction for accountId: {}, saved with id: {}", event.accountId(), savedTransaction.getId());
    }

    private AccountTransaction buildAccountTransactionObject(Account account, BigDecimal amount, BigDecimal balance) {
        return AccountTransaction.builder()
                .transactionDate(account.getCreatedAt())
                .amount(amount)
                .balanceBeforeTransaction(balance.subtract(amount))
                .balanceAfterTransaction(balance)
                .account(account)
                .build();
    }

    @Override
    public BigDecimal getAverageBalanceFromDeclaredMonths(String userId, int declaredMonths) {
        InitDate initDate = getInitDate(declaredMonths);
        double doubleAverage = getAverage(userId, initDate);
        return BigDecimal.valueOf(doubleAverage);
    }

    private double getAverage(String userId, InitDate initDate) {
        return accountTransactionRepository.findAllByAccount_UserIdAndTransactionDateIsBetween(
                        userId,
                        initDate.startDate(),
                        initDate.endDate())
                .stream()
                .collect(Collectors.groupingBy(
                        transaction ->
                                transaction.transactionDate()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM"))))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    return entry.getValue()
                            .stream()
                            .map(AccountTransactionCalculationDto::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(entry.getValue().size()));
                })).values()
                .stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElseThrow();
    }

    private InitDate getInitDate(int declaredMonths) {
        LocalDateTime initEndDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime initStartDate = initEndDate.minusMonths(declaredMonths);

        LocalDateTime endDate = LocalDateTime.of(
                initEndDate.getYear(),
                initEndDate.getMonth(),
                initEndDate.getMonth().maxLength(),
                23,
                59,
                59);
        LocalDateTime startDate = LocalDateTime.of(
                initStartDate.getYear(),
                initStartDate.getMonth(),
                initStartDate.getMonth().minLength(),
                0,
                0,
                0);
        return new InitDate(endDate, startDate);
    }

    private record InitDate(LocalDateTime endDate, LocalDateTime startDate) {
    }

}
