package pl.dk.exchange_service.httpclient.dtos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UpdateAccountBalanceDto(
        @NotBlank
        @NotNull
        String currencyType,
        @NotNull
        BigDecimal updateByValue
) {
}
