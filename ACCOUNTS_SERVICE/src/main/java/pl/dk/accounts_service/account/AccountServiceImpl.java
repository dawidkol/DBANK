package pl.dk.accounts_service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.exception.UserNotFoundException;
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
        checkIfUserExists(createAccountDto);
        Account accountToSave = AccountDtoMapper.map(createAccountDto);
        BigInteger accountNumber = accountNumberGenerator.generateAccountNumber();
        accountToSave.setAccountNumber(accountNumber);
        Account savedAccount = accountRepository.save(accountToSave);
        return AccountDtoMapper.map(savedAccount);
    }

    private void checkIfUserExists(CreateAccountDto createAccountDto) {
        String userId = createAccountDto.userId();
        ResponseEntity<UserDto> userById = userFeignClient.getUserById(userId);
        boolean sameCodeAs = userById.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND);
        if (sameCodeAs) {
            throw new UserNotFoundException("User with id = %s not found".formatted(userId));
        }
    }
}
