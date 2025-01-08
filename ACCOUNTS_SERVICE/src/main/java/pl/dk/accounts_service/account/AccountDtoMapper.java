package pl.dk.accounts_service.account;

import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.enums.AccountType;

class AccountDtoMapper {

    public static Account map(CreateAccountDto createAccountDto) {
        AccountType accountType = Enum.valueOf(AccountType.class, createAccountDto.accountType().toUpperCase());
        return Account.builder()
                .accountType(accountType)
                .userId(createAccountDto.userId())
                .active(true)
                .build();
    }

    public static AccountDto map(Account account) {
        return AccountDto.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().toString())
                .userId(account.getUserId())
                .active(account.getActive())
                .build();
    }
}
