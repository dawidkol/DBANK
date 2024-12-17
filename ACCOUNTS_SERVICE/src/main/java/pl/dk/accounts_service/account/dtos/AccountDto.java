package pl.dk.accounts_service.account.dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;

@Builder
public record AccountDto(
        BigInteger accountNumber,
        String accountType,
        BigDecimal balance,
        String userId
) {
}
