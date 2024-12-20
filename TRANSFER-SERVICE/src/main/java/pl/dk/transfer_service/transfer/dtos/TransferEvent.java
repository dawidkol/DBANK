package pl.dk.transfer_service.transfer.dtos;

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