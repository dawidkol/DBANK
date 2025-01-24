package pl.dk.cardservice.httpclient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.cardservice.exception.FeignClientException;
import pl.dk.cardservice.httpclient.dto.AccountDto;

@Component
class AccountServiceFeignClientImpl implements AccountServiceFeignClient {

    @Override
    public ResponseEntity<AccountDto> getAccountById(String accountId) {
        throw new FeignClientException("Account-Service unavailable");
    }
}
