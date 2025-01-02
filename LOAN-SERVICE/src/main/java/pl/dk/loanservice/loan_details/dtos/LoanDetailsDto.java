package pl.dk.loanservice.loan_details.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LoanDetailsDto(String loanId,
                             String userId,
                             BigDecimal amount,
                             BigDecimal remainingAmount,
                             BigDecimal amountPaid,
                             String loanAccount,
                             LocalDate lastPaymentDateTo,
                             Boolean scheduleAvailable
) {
}
