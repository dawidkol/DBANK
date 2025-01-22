package pl.dk.exchange_service.httpclient.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record Rates(
        String no,
        LocalDate effectiveDate,
        BigDecimal bid,
        BigDecimal ask
) {
}
