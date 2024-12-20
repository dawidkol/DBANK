package pl.dk.transfer_service.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;

@FeignClient(value = "user-service", fallback = AccountFallback.class, dismiss404 = true)
public interface AccountFeignClient {

    @GetMapping("/{accountId}")
    ResponseEntity<AccountDto> getAccountById(@PathVariable String accountId);

}
