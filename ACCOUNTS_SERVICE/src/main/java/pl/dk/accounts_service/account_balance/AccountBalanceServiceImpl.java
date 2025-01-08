package pl.dk.accounts_service.account_balance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.AccountRepository;
import pl.dk.accounts_service.account.dtos.AccountEventPublisher;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;
import pl.dk.accounts_service.enums.CurrencyType;
import pl.dk.accounts_service.exception.AccountBalanceException;
import pl.dk.accounts_service.exception.AccountBalanceNotFoundException;
import pl.dk.accounts_service.exception.AccountInactiveException;
import pl.dk.accounts_service.exception.AccountNotExistsException;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
class AccountBalanceServiceImpl implements AccountBalanceService {

    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public AccountBalanceDto updateAccountBalance(String accountNumber, UpdateAccountBalanceDto updateAccountBalanceDto) {
        Account account = accountRepository.findById(accountNumber).orElseThrow(() ->
                new AccountNotExistsException("Account with accountNumber: %s not exists".formatted(accountNumber)));
        isAccountActive(account);

        CurrencyType currencyType = CurrencyType.valueOf(updateAccountBalanceDto.currencyType().toUpperCase());
        BigDecimal amount = updateAccountBalanceDto.updateByValue();
        AtomicReference<AccountBalanceDto> balanceDtoAtomicReference = new AtomicReference<>();
        accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType)
                .ifPresentOrElse(accountBalance -> {
                    AccountBalance ac = validateAndUpdateAccountBalance(updateAccountBalanceDto.updateByValue(), accountBalance);
                    balanceDtoAtomicReference.set(AccountBalanceDtoMapper.map(ac));
                }, () -> {
                    int result = updateAccountBalanceDto.updateByValue().compareTo(BigDecimal.ZERO);
                    if (result < 0) {
                        throw new AccountBalanceException("Account balance for: %s can't be less than O"
                                .formatted(account.getAccountNumber()));
                    }
                    AccountBalance savedAccountBalance = createAndSaveNewAccountBalance(amount, account, currencyType);
                    balanceDtoAtomicReference.set(AccountBalanceDtoMapper.map(savedAccountBalance));
                });
        AccountBalanceDto result = balanceDtoAtomicReference.get();
        buildAndPublishAccountEvent(currencyType, amount, accountNumber);
        return result;

    }

    private void isAccountActive(Account account) {
        if (!account.getActive()) {
            throw new AccountInactiveException("Account with number: %s is inactive".formatted(account.getAccountNumber()));
        }
    }

    private AccountBalance createAndSaveNewAccountBalance(BigDecimal amount, Account account, CurrencyType currencyType) {
        AccountBalance accountBalanceToSave = AccountBalance.builder()
                .account(account)
                .balance(amount)
                .currencyType(currencyType)
                .build();
        return accountBalanceRepository.save(accountBalanceToSave);
    }

    private void buildAndPublishAccountEvent(CurrencyType currencyType, BigDecimal updateByValue, String accountId) {
        AccountEventPublisher accountEventPublisher = AccountEventPublisher.builder()
                .currencyType(currencyType)
                .updatedByValue(updateByValue)
                .accountId(accountId)
                .build();
        applicationEventPublisher.publishEvent(accountEventPublisher);
    }

    private AccountBalance validateAndUpdateAccountBalance(BigDecimal updateByValue, AccountBalance accountBalance) {
        BigDecimal currentBalance = accountBalance.getBalance();
        int isLowerThanZero = updateByValue.compareTo(BigDecimal.ZERO);
        if (isLowerThanZero < 0) {
            BigDecimal absAmount = updateByValue.abs();
            int result = currentBalance.compareTo(absAmount);
            if (result < 0) {
                throw new AccountBalanceException("Account balance for: %s can't be less than O"
                        .formatted(accountBalance.getAccount().getAccountNumber()));
            }
        }
        BigDecimal updatedAccountBalance = currentBalance.add(updateByValue);
        accountBalance.setBalance(updatedAccountBalance);
        return accountBalance;
    }

    @Override
    public AccountBalanceDto getAccountBalanceByAccountNumberAndCurrencyType(String accountNumber, String currencyType) {
        CurrencyType cT = CurrencyType.valueOf(currencyType.toUpperCase());
        return accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, cT)
                .map(AccountBalanceDtoMapper::map)
                .orElseThrow(() ->
                        new AccountBalanceNotFoundException("AccountBalance with accountNumber %s and currencyType %s not found"
                                .formatted(accountNumber, currencyType))
                );
    }
}
