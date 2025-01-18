package pl.dk.exchange_service.currency.dtos;

import lombok.Builder;
import pl.dk.exchange_service.enums.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CurrencyDto(String currencyId,
                          String name,
                          CurrencyType currencyType,
                          LocalDate effectiveDate,
                          BigDecimal bid,
                          BigDecimal ask) {
}
