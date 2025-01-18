package pl.dk.transfer_service.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dk.transfer_service.httpClient.dtos.AccountBalanceDto;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;

@FeignClient(value = "account-service", fallback = AccountFallback.class, dismiss404 = true)
public interface AccountFeignClient {

    @GetMapping("/accounts/{accountId}")
    ResponseEntity<AccountDto> getAccountById(@PathVariable String accountId);

    @GetMapping("/accounts/{accountNumber}/balance")
    ResponseEntity<AccountBalanceDto> getAccountBalanceByAccountNumberAndCurrencyType(@PathVariable String accountNumber,
                                                                                      @RequestParam String currencyType);

}
