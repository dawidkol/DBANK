package pl.dk.transfer_service.transfer.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateTransferDto(
        @Pattern(regexp = "\\d{26}")
        String senderAccountNumber,
        @Pattern(regexp = "\\d{26}")
        String recipientAccountNumber,
        @Positive
        BigDecimal amount,
        @NotBlank
        String currencyType,
        @FutureOrPresent
        LocalDateTime transferDate,
        @NotBlank
        @NotBlank
        @Size(min = 5, max = 300)
        String description
) {
}
