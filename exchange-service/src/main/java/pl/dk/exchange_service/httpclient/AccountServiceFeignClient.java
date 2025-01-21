package pl.dk.exchange_service.httpclient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import pl.dk.exchange_service.httpclient.dtos.dtos.AccountBalanceDto;
import pl.dk.exchange_service.httpclient.dtos.dtos.UpdateAccountBalanceDto;

@Component
@FeignClient(value = "account-service", dismiss404 = true, fallbackFactory = AccountServiceFallbackFactory.class)
public interface AccountServiceFeignClient {

    @PatchMapping("/accounts/{accountNumber}")
    ResponseEntity<AccountBalanceDto> updateBalance(@PathVariable String accountNumber,
                                                    @Valid @RequestBody UpdateAccountBalanceDto updateAccountBalanceDto);
}
