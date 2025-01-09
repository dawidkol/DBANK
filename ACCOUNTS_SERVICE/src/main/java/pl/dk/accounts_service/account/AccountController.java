package pl.dk.accounts_service.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.account_balance.AccountBalanceService;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;

import java.net.URI;
import java.util.List;

import static pl.dk.accounts_service.constants.PagingAndSorting.*;
import static pl.dk.accounts_service.constants.PagingAndSorting.SIZE_DEFAULT;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
class AccountController {

    private final AccountService accountService;
    private final AccountBalanceService accountBalanceService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountDto createAccountDto) {
        AccountDto account = accountService.createAccount(createAccountDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(account.accountNumber())
                .toUri();
        return ResponseEntity.created(uri).body(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable String accountId) {
        AccountDto accountById = accountService.getAccountById(accountId);
        return ResponseEntity.ok(accountById);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccountById(@PathVariable String accountId) {
        accountService.deleteAccountById(accountId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<AccountBalanceDto> updateBalance(@PathVariable String accountNumber,
                                                           @Valid @RequestBody UpdateAccountBalanceDto updateAccountBalanceDto) {
        AccountBalanceDto accountBalanceDto = accountBalanceService.updateAccountBalance(accountNumber, updateAccountBalanceDto);
        return ResponseEntity.ok(accountBalanceDto);
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<AccountDto>> getAllUserAccounts(@PathVariable String userId,
                                                               @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
                                                               @RequestParam(required = false, defaultValue = SIZE_DEFAULT) int size) {
        List<AccountDto> allUserAccounts = accountService.getAllUserAccounts(userId, page, size);
        return ResponseEntity.ok(allUserAccounts);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceDto> getAccountBalanceByAccountNumberAndCurrencyType(@PathVariable String accountNumber,
                                                                                             @RequestParam String currencyType) {
        AccountBalanceDto accountBalanceByAccountNumberAndCurrencyType = accountBalanceService.getAccountBalanceByAccountNumberAndCurrencyType(accountNumber, currencyType);
        return ResponseEntity.ok(accountBalanceByAccountNumberAndCurrencyType);
    }

}
