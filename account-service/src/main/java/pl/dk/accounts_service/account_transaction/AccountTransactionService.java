package pl.dk.accounts_service.account_transaction;

import pl.dk.accounts_service.account.dtos.AccountEventPublisher;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionDto;

import java.math.BigDecimal;

interface AccountTransactionService {

    AccountTransactionDto saveTransaction(AccountTransaction transaction);
    BigDecimal getAverageBalanceFromDeclaredMonths(String userId, int declaredMonths);
    void onTransactionEvent(AccountEventPublisher event);

}
