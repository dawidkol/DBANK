package pl.dk.accounts_service.account.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import pl.dk.accounts_service.error.AccountTypeEnum;

import java.math.BigDecimal;

@Builder
public record CreateAccountDto(
        @AccountTypeEnum
        String accountType,
        @PositiveOrZero
        @NotNull
        BigDecimal balance,
        @NotBlank
        String userId

) {
}
