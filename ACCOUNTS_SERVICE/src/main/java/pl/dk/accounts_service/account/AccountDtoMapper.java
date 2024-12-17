package pl.dk.accounts_service.account;

import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;

import java.math.BigDecimal;

class AccountDtoMapper {

    public static Account map(CreateAccountDto createAccountDto) {
        AccountType accountType = Enum.valueOf(AccountType.class, createAccountDto.accountType());
        return Account.builder()
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .userId(createAccountDto.userId())
                .build();
    }

    public static AccountDto map(Account account) {
        return AccountDto.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().toString())
                .balance(account.getBalance())
                .userId(account.getUserId())
                .build();
    }
}
