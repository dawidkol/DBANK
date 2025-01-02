package pl.dk.loanservice.httpClient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.dk.loanservice.loan.dtos.CreateTransferDto;
import pl.dk.loanservice.loan.dtos.TransferDto;

@FeignClient(name = "TRANSFER-SERVICE", fallbackFactory = TransferServiceFallbackFactory.class, dismiss404 = true)
public interface TransferServiceFeignClient {

    @PostMapping("/transfers")
    ResponseEntity<TransferDto> createTransfer(@Valid @RequestBody CreateTransferDto createTransferDto);
}
