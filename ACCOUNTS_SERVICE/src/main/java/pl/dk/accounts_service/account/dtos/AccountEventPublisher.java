package pl.dk.accounts_service.account.dtos;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AccountEventPublisher(BigDecimal updatedByValue, String accountId) {
}
