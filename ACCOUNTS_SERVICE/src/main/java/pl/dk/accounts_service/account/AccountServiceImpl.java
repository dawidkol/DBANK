package pl.dk.accounts_service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.exception.AccountAlreadyExistsException;
import pl.dk.accounts_service.exception.AccountNotExistsException;
import pl.dk.accounts_service.exception.UserNotFoundException;
import pl.dk.accounts_service.exception.UserServiceUnavailableException;
import pl.dk.accounts_service.httpClient.UserFeignClient;
import pl.dk.accounts_service.httpClient.dtos.UserDto;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public AccountDto createAccount(CreateAccountDto createAccountDto) {
        checkUserServiceResponse(createAccountDto);
        Account accountToSave = AccountDtoMapper.map(createAccountDto);
        String accountNumber = accountNumberGenerator.generateAccountNumber();
        accountToSave.setAccountNumber(accountNumber);
        Account savedAccount = accountRepository.save(accountToSave);
        return AccountDtoMapper.map(savedAccount);
    }

    private void checkUserServiceResponse(CreateAccountDto createAccountDto) {
        String userId = createAccountDto.userId();
        ResponseEntity<UserDto> userById = userFeignClient.getUserById(userId);
        boolean notFound = userById.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND);
        if (notFound) {
            throw new UserNotFoundException("User with id = %s not found".formatted(userId));
        }
    }

    @Override
    public AccountDto getAccountById(String accountId) {
        return accountRepository.findById(accountId)
                .map(AccountDtoMapper::map)
                .orElseThrow(() -> {
                    throw new AccountNotExistsException("Account with id: %s not exists");
                });
    }
}
