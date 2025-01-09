package pl.dk.accounts_service.account.dtos;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record AccountDto(
        String accountNumber,
        String accountType,
        String userId,
        Boolean active
) {
}
