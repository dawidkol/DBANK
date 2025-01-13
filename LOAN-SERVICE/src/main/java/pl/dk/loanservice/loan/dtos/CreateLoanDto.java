package pl.dk.loanservice.loan.dtos;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CreateLoanDto(

        @NotNull
        @NotBlank
        @UUID
        String userId,
        @Positive
        @NotNull
        BigDecimal amount,
        @Positive
        @NotNull
        BigDecimal interestRate,
        @FutureOrPresent
        @NotNull
        LocalDate startDate,
        @Future
        @NotNull
        LocalDate endDate,
        @NotBlank
        @NotNull
        @Size(min = 10, max = 300)
        String description,
        @NotNull
        @Positive
        BigDecimal avgIncome,
        @NotNull
        @Positive
        BigDecimal avgExpenses,
        @NotNull
        @PositiveOrZero
        BigDecimal existingLoanRepayments,
        @NotNull
        @NotBlank
        String currencyType
) {
}
