package pl.dk.accounts_service.account.dtos;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record AccountDto(
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String userId
) {
}
