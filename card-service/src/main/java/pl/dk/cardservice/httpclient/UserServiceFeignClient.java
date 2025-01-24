package pl.dk.cardservice.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dk.cardservice.httpclient.dto.UserDto;

@FeignClient(value = "user-service", dismiss404 = true, fallback = UserServiceFeignClientImpl.class)
public interface UserServiceFeignClient {

    @GetMapping("/users/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable String userId);

}
