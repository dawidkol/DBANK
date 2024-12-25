package pl.dk.loanservice.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(value = "accounts-service", fallback = AccountServiceFallback.class, dismiss404 = true)
public interface AccountServiceFeignClient {

    @GetMapping("/transactions/last-12-months/{userId}")
    ResponseEntity<BigDecimal> getAvgLast12Moths(@PathVariable String userId);

}
