package pl.dk.notification_service.httpClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.notification_service.exception.UserServiceUnavailableException;
import pl.dk.notification_service.kafka.consumer.dtos.UserDto;

@Component
@Slf4j
class UserServiceFeignClientImpl implements UserServiceFeignClient {

    @Override
    public ResponseEntity<UserDto> getUserById(String userId) {
        return null;
    }
}
