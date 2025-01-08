package pl.dk.accounts_service.account_balance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.Account;
import pl.dk.accounts_service.account.AccountRepository;
import pl.dk.accounts_service.account.AccountType;
import pl.dk.accounts_service.account.dtos.UpdateAccountBalance;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;
import pl.dk.accounts_service.exception.AccountNotExistsException;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
class AccountBalanceServiceImpl implements AccountBalanceService {

    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountBalanceDto updateAccountBalance(String accountNumber, UpdateAccountBalanceDto updateAccountBalanceDto) {
        Account account = accountRepository.findById(accountNumber).orElseThrow(() ->
                new AccountNotExistsException("Account with accountNumber: %s not exists".formatted(accountNumber)));

        CurrencyType currencyType = CurrencyType.valueOf(updateAccountBalanceDto.currencyType().toUpperCase());
        AtomicReference<AccountBalanceDto> balanceDtoAtomicReference = new AtomicReference<>();
        accountBalanceRepository.findFirstByAccount_AccountNumberAndCurrencyType(accountNumber, currencyType)
                .ifPresentOrElse(accountBalance -> {
                    BigDecimal newValue = accountBalance.getBalance().add(updateAccountBalanceDto.amount());
                    accountBalance.setBalance(newValue);
                }, () -> {
                    AccountBalance savedAccountBalance = createAndSaveNewAccountBalance(updateAccountBalanceDto, account, currencyType);
                    balanceDtoAtomicReference.set(AccountBalanceDtoMapper.map(savedAccountBalance));

                });
        return balanceDtoAtomicReference.get();
    }

    private AccountBalance createAndSaveNewAccountBalance(UpdateAccountBalanceDto updateAccountBalanceDto, Account account, CurrencyType currencyType) {
        AccountBalance accountBalanceToSave = AccountBalance.builder()
                .account(account)
                .balance(updateAccountBalanceDto.amount())
                .currencyType(currencyType)
                .build();
        return accountBalanceRepository.save(accountBalanceToSave);
    }
}
