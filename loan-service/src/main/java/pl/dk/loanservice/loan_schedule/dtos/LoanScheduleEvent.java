package pl.dk.loanservice.loan_schedule.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LoanScheduleEvent(BigDecimal amount,
                                BigDecimal interestRate,
                                LocalDate startDate,
                                LocalDate endDate,
                                Integer numberOfInstallments,
                                String loanId) {
}
