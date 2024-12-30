package pl.dk.accounts_service.account;



import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.AccountEventPublisher;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.exception.AccountBalanceException;
import pl.dk.accounts_service.exception.AccountInactiveException;
import pl.dk.accounts_service.exception.AccountNotExistsException;
import pl.dk.accounts_service.exception.UserNotFoundException;
import pl.dk.accounts_service.httpClient.UserFeignClient;
import pl.dk.accounts_service.httpClient.dtos.UserDto;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final UserFeignClient userFeignClient;
    private final ApplicationEventPublisher applicationEventPublisher;

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
                .orElseThrow(() ->
                        new AccountNotExistsException("Account with id: %s not exists".formatted(accountId)));
    }

    @Transactional
    @Override
    public void deleteAccountById(String accountId) {
        accountRepository.findById(accountId).ifPresentOrElse(
                account -> {
                    account.setActive(false);
                }, () -> {
                    throw new AccountNotExistsException("Account with id: %s not exists");
                });
    }

    @Override
    @Transactional
    public AccountDto updateAccountBalance(String accountId, BigDecimal updateByValue) {
        AccountDto accountDto = accountRepository.findById(accountId)
                .map(account -> {
                    isAccountActive(account);
                    updateAccountBalance(updateByValue, account);
                    return AccountDtoMapper.map(account);
                })
                .orElseThrow(() ->
                        new AccountNotExistsException("Account with id: %s not exists"));
        buildAndPublishAccountEvent(updateByValue, accountDto.accountNumber());
        return accountDto;
    }

    private void buildAndPublishAccountEvent(BigDecimal updateByValue, String accountId) {
        AccountEventPublisher accountEventPublisher = AccountEventPublisher.builder()
                .updatedByValue(updateByValue)
                .accountId(accountId)
                .build();
        applicationEventPublisher.publishEvent(accountEventPublisher);
    }

    private void updateAccountBalance(BigDecimal updateByValue, Account account) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal absAmount = updateByValue.abs();
        int result = currentBalance.compareTo(absAmount);
        if (result < 0) {
            throw new AccountBalanceException("Insufficient funds in the account");
        }
        BigDecimal updatedAccountBalance = currentBalance.add(updateByValue);
        account.setBalance(updatedAccountBalance);
    }

    private void isAccountActive(Account account) {
        if (!account.getActive()) {
            throw new AccountInactiveException("Account with number: %s is inactive".formatted(account.getAccountNumber()));
        }
    }

    @Override
    public List<AccountDto> getAllUserAccounts(String userId, int page, int size) {
        return accountRepository.findAllByUserId(userId, PageRequest.of(page - 1, size))
                .stream()
                .map(AccountDtoMapper::map)
                .toList();
    }
}
