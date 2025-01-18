package pl.dk.transfer_service.kafka.consumer.dtos;

import lombok.Builder;
import pl.dk.transfer_service.enums.TransferStatus;

import java.time.LocalDateTime;

@Builder
public record LoanTransferDto(
        String id,
        LocalDateTime transferDate,
        TransferStatus transferStatus
) {
}
