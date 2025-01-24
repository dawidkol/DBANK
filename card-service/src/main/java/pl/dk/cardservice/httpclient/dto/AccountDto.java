package pl.dk.cardservice.httpclient.dto;

import lombok.Builder;

@Builder
public record AccountDto(
        String accountNumber,
        String accountType,
        String userId,
        Boolean active
) {
}
