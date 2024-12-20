package pl.dk.transfer_service.httpClient.dtos;

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
