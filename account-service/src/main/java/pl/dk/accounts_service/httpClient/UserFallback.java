package pl.dk.accounts_service.httpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.accounts_service.exception.UserServiceUnavailableException;
import pl.dk.accounts_service.httpClient.dtos.UserDto;

@Component
class UserFallback implements UserFeignClient {

    @Override
    public ResponseEntity<UserDto> getUserById(String userId) {
        throw new UserServiceUnavailableException("USER-SERVICE unavailable, try again later");
    }
}
