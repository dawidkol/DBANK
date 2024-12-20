package pl.dk.transfer_service.httpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.transfer_service.exception.AccountServiceUnavailableException;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;

@Component
class AccountFallback implements AccountFeignClient {

    @Override
    public ResponseEntity<AccountDto> getAccountById(String accountId) {
        throw new AccountServiceUnavailableException("ACCOUNT-SERVICE unavailable, try again later");
    }
}
