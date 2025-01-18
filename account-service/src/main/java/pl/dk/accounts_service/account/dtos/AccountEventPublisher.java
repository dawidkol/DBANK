package pl.dk.accounts_service.account.dtos;

import lombok.Builder;
import pl.dk.accounts_service.enums.CurrencyType;

import java.math.BigDecimal;

@Builder
public record AccountEventPublisher(CurrencyType currencyType, BigDecimal updatedByValue, String accountId) {
}
