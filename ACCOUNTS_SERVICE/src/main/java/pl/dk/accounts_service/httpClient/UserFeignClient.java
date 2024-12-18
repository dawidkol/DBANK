package pl.dk.accounts_service.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dk.accounts_service.httpClient.dtos.UserDto;

@FeignClient(value = "user-service", fallback = UserFallback.class, dismiss404 = true)
public interface UserFeignClient {

    @GetMapping("/users/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable String userId);

}
