package pl.dk.cardservice.httpclient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.cardservice.exception.FeignClientException;
import pl.dk.cardservice.httpclient.dto.UserDto;

@Component
class UserServiceFeignClientImpl implements UserServiceFeignClient {

    @Override
    public ResponseEntity<UserDto> getUserById(String userId) {
        throw new FeignClientException("User Service Unavailable");
    }
}
