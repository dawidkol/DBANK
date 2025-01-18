package pl.dk.transfer_service.transfer.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransferDto(
        String transferId,
        String senderAccountNumber,
        String recipientAccountNumber,
        BigDecimal amount,
        String currencyType,
        LocalDateTime transferDate,
        String transferStatus,
        String description,
        BigDecimal balanceAfterTransfer) {
}
