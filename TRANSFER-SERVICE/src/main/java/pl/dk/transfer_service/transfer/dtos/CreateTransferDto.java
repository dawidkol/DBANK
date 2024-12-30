package pl.dk.transfer_service.transfer.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateTransferDto(
        @NotNull
        @Pattern(regexp = "\\d{26}")
        String senderAccountNumber,
        @NotNull
        @Pattern(regexp = "\\d{26}")
        String recipientAccountNumber,
        @NotNull
        @Positive
        BigDecimal amount,
        @NotNull
        @NotBlank
        String currencyType,
        @NotNull
        @FutureOrPresent
        LocalDateTime transferDate,
        @NotNull
        @NotBlank
        @Size(min = 5, max = 300)
        String description
) {
}
