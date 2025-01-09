package pl.dk.accounts_service.account_balance;

import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;

class AccountBalanceDtoMapper {

    public static AccountBalanceDto map(AccountBalance accountBalance) {
        return AccountBalanceDto.builder()
                .accountBalanceId(accountBalance.getId())
                .currencyType(accountBalance.getCurrencyType())
                .balance(accountBalance.getBalance())
                .build();
    }
}
