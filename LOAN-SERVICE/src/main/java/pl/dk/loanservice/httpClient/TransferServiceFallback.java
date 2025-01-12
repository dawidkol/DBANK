package pl.dk.loanservice.httpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.dk.loanservice.exception.TransferServiceUnavailableException;
import pl.dk.loanservice.httpClient.dtos.LoanTransferDto;
import pl.dk.loanservice.loan.dtos.CreateTransferDto;
import pl.dk.loanservice.loan.dtos.TransferDto;

import java.util.Collection;
import java.util.List;

@Component
class TransferServiceFallback implements TransferServiceFeignClient {

    @Override
    public ResponseEntity<TransferDto> createTransfer(CreateTransferDto createTransferDto) {
        throw new TransferServiceUnavailableException("Transfer Service unavailable");
    }

    @Override
    public ResponseEntity<List<LoanTransferDto>> getTransfersByIds(Collection<String> ids, int page, int size) {
        throw new TransferServiceUnavailableException("Transfer Service unavailable");
    }
}
