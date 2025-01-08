package pl.dk.accounts_service.account_balance.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UpdateAccountBalanceDto(
        @NotBlank
        @NotNull
        String currencyType,
        @NotBlank
        @NotNull
        BigDecimal updateByValue
) {
}
