package pl.dk.loanservice.httpClient.dtos;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record AccountDto(
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String userId,
        Boolean active
) {
}
