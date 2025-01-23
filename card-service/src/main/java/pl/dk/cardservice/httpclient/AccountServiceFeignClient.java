package pl.dk.cardservice.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dk.cardservice.httpclient.dto.AccountDto;

@FeignClient(value = "account-service", dismiss404 = true, fallback = AccountServiceFeignClientImpl.class )
public interface AccountServiceFeignClient {

    @GetMapping("/accounts/{accountId}")
    ResponseEntity<AccountDto> getAccountById(@PathVariable String accountId);

}
