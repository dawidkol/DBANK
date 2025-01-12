package pl.dk.loanservice.loan.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LoanDto(
        String id,
        String userId,
        BigDecimal amount,
        BigDecimal interestRate,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal remainingAmount,
        String status,
        String description,
        String currencyType
) {
}
