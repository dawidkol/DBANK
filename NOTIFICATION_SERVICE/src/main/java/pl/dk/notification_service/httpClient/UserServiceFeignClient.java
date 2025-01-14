package pl.dk.notification_service.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dk.notification_service.kafka.consumer.dtos.UserDto;

@FeignClient(value = "user-service", dismiss404 = true)
public interface UserServiceFeignClient {

    @GetMapping("/users/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable String userId);
}
