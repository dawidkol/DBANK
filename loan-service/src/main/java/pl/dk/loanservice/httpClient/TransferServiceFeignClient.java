package pl.dk.loanservice.httpClient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dk.loanservice.httpClient.dtos.LoanTransferDto;
import pl.dk.loanservice.loan.dtos.CreateTransferDto;
import pl.dk.loanservice.loan.dtos.TransferDto;

import java.util.Collection;
import java.util.List;

@FeignClient(name = "transfer-service", fallbackFactory = TransferServiceFallbackFactory.class, dismiss404 = true)
public interface TransferServiceFeignClient {

    @PostMapping("/transfers")
    ResponseEntity<TransferDto> createTransfer(@Valid @RequestBody CreateTransferDto createTransferDto);

    @GetMapping("/transfers/records}")
    ResponseEntity<List<LoanTransferDto>> getTransfersByIds(@RequestParam Collection<String> ids,
                                                            @RequestParam int page,
                                                            @RequestParam int size);
}
