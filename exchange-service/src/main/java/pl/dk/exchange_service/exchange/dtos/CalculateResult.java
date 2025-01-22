package pl.dk.exchange_service.exchange.dtos;

import java.math.BigDecimal;

public record CalculateResult(BigDecimal amountToSubtract, BigDecimal amountToAdd, BigDecimal rate) {
}
