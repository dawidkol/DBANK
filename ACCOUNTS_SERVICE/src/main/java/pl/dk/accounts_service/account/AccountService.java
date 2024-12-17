package pl.dk.accounts_service.account;

import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;

interface AccountService {

    AccountDto createAccount(CreateAccountDto createAccountDto);
    AccountDto getAccountById(String accountId);
}
