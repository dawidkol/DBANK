package pl.dk.accounts_service.account_transaction;

import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionDto;

class AccountTransactionDtoMapper {

    public static AccountTransactionDto map(AccountTransaction accountTransaction) {
        return AccountTransactionDto.builder()
                .id(accountTransaction.getId())
                .transactionDate(accountTransaction.getTransactionDate())
                .amount(accountTransaction.getAmount())
                .balanceBeforeTransaction(accountTransaction.getBalanceBeforeTransaction())
                .balanceAfterTransaction(accountTransaction.getBalanceAfterTransaction())
                .accountNumber(accountTransaction.getAccount().getAccountNumber())
                .build();
    }
}
