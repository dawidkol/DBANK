package pl.dk.accounts_service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor

class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    @Transactional
    public AccountDto createAccount(CreateAccountDto createAccountDto) {
        Account accountToSave = AccountDtoMapper.map(createAccountDto);
        BigInteger accountNumber = accountNumberGenerator.generateAccountNumber();
        accountToSave.setAccountNumber(accountNumber);
        Account savedAccount = accountRepository.save(accountToSave);
        return AccountDtoMapper.map(savedAccount);
    }
}
