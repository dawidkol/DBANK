package pl.dk.accounts_service.account_transaction;

import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionDto;

public interface AccountTransactionService {

    AccountTransactionDto saveTransaction(AccountTransaction transaction);
}
