package pl.dk.loanservice.httpClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "accounts-service")
interface AccountServiceFeignClient {
}
