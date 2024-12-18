package pl.dk.accounts_service.account;

import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;

import java.math.BigDecimal;

interface AccountService {

    AccountDto createAccount(CreateAccountDto createAccountDto);
    AccountDto getAccountById(String accountId);
    void deleteAccountById(String accountId);
    AccountDto updateAccountBalance(String accountId, BigDecimal amount);
}
