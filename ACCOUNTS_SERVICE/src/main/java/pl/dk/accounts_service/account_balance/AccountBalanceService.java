package pl.dk.accounts_service.account_balance;

import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;

public interface AccountBalanceService {

    AccountBalanceDto updateAccountBalance(String accountNumber, UpdateAccountBalanceDto updateAccountBalanceDto);
}
