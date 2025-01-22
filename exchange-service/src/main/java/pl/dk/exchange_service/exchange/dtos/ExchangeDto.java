package pl.dk.exchange_service.exchange.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import pl.dk.exchange_service.constraint.CurrencyTypeConstraint;
import pl.dk.exchange_service.enums.CurrencyType;

import java.math.BigDecimal;

@Builder
public record ExchangeDto(
        @NotNull
        @NotBlank
        String accountNumber,
        @CurrencyTypeConstraint
        CurrencyType currencyFrom,
        @CurrencyTypeConstraint
        CurrencyType currencyTo,
        @Positive
        BigDecimal valueFrom
) {
}
