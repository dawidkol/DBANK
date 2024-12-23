package pl.dk.accounts_service.account_transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.AccountRepository;
import pl.dk.accounts_service.account.dtos.AccountEventPublisher;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionDto;
import pl.dk.accounts_service.exception.AccountNotExistsException;

import java.math.BigDecimal;

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

    @ApplicationModuleListener
    void onUpdateEvent(AccountEventPublisher event) {
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
}
