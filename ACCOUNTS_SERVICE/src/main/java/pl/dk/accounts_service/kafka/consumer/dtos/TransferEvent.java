package pl.dk.accounts_service.kafka.consumer.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransferEvent(
        String transferId,
        String senderAccountNumber,
        String recipientAccountNumber,
        BigDecimal amount,
        String currencyType,
        LocalDateTime transferDate,
        String transferStatus) {
}
