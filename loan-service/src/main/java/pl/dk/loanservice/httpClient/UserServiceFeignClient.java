package pl.dk.loanservice.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dk.loanservice.httpClient.dtos.UserDto;

@FeignClient(value = "user-service", fallback = UserServiceFallback.class, dismiss404 = true)
public interface UserServiceFeignClient {

    @GetMapping("/users/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable String userId);

}
