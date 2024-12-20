package pl.dk.transfer_service.transfer.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateTransferDto(
        String senderAccountNumber,
        String recipientAccountNumber,
        BigDecimal amount,
        String currencyType,
        LocalDateTime transferDate,
        String description
) {
}
