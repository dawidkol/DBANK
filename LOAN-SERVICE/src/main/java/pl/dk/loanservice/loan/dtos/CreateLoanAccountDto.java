package pl.dk.loanservice.loan.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import pl.dk.loanservice.error.AccountTypeEnum;

import java.math.BigDecimal;

@Builder
public record CreateLoanAccountDto(
        @AccountTypeEnum
        String accountType,
        @PositiveOrZero
        @NotNull
        BigDecimal balance,
        @NotBlank
        String userId,
        @NotBlank
        String loanId
) {

}
