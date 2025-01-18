package pl.dk.loanservice.kafka.consumer.dtos;

import lombok.Builder;
import pl.dk.loanservice.enums.TransferStatus;

import java.time.LocalDateTime;

@Builder
public record LoanTransferDto(
        String id,
        LocalDateTime transferDate,
        TransferStatus transferStatus
) {
}
