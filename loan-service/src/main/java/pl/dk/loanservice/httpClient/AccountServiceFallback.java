package pl.dk.loanservice.httpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.loanservice.exception.AccountServiceUnavailableException;
import pl.dk.loanservice.httpClient.dtos.AccountDto;

import java.math.BigDecimal;

@Component
class AccountServiceFallback implements AccountServiceFeignClient {

    @Override
    public ResponseEntity<BigDecimal> getAvgLast12Moths(String userId) {
        throw new AccountServiceUnavailableException("Account-Service unavailable");
    }

    @Override
    public ResponseEntity<AccountDto> getAccountById(String accountId) {
        throw new AccountServiceUnavailableException("Account-Service unavailable");
    }
}
