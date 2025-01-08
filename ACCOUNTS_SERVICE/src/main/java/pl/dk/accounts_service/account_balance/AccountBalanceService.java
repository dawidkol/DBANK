package pl.dk.accounts_service.account_balance;

import pl.dk.accounts_service.account.dtos.UpdateAccountBalance;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;

interface AccountBalanceService {

    AccountBalanceDto updateAccountBalance(String accountNumber, UpdateAccountBalanceDto updateAccountBalanceDto);
}
