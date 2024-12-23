package pl.dk.loanservice.loan.dtos;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.UUID;
import pl.dk.loanservice.loan.LoanStatus;

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
        String description
) {
}
