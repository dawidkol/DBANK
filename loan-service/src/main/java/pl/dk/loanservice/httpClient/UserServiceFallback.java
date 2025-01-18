package pl.dk.loanservice.httpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.loanservice.exception.UserServiceUnavailableException;
import pl.dk.loanservice.httpClient.dtos.UserDto;

@Component
class UserServiceFallback implements UserServiceFeignClient {

    @Override
    public ResponseEntity<UserDto> getUserById(String userId) {
        throw new UserServiceUnavailableException("USER-SERVICE unavailable, try again later");
    }
}
