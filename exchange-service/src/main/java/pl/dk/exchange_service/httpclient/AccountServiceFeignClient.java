package pl.dk.exchange_service.httpclient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pl.dk.exchange_service.exception.AccountServiceUnavailable;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;
import pl.dk.exchange_service.httpclient.dtos.dtos.UpdateAccountBalanceDto;

@Component
@FeignClient(value = "account-service", dismiss404 = true, fallbackFactory = AccountServiceFallbackFactory.class)
public interface AccountServiceFeignClient {

    @Retryable(recover = "processUpdateAccountRequestRecover",
            retryFor = Exception.class, maxAttempts = 5,
            backoff = @Backoff(delay = 2000L, multiplier = 2))
    @PatchMapping("/accounts/{accountNumber}")
    ResponseEntity<AccountBalanceDto> updateBalance(@PathVariable String accountNumber,
                                                    @Valid @RequestBody UpdateAccountBalanceDto updateAccountBalanceDto);

    @Recover
    default ResponseEntity<AccountBalanceDto> processUpdateAccountRequestRecover(Exception e,
                                                                                 String accountNumber,
                                                                                 UpdateAccountBalanceDto updateAccountBalanceDto) {
        throw new AccountServiceUnavailable("Account-Service Unavailable, try again later");
    }
}
