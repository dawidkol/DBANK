package pl.dk.accounts_service.account.dtos;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateAccountDto(
        String accountType,
        BigDecimal balance,
        String userId

) {
}
