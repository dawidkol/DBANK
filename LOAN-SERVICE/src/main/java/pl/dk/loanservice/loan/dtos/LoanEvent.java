package pl.dk.loanservice.loan.dtos;

import lombok.Builder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Builder
public record LoanEvent(String userId, String loanId, BigDecimal avgIncome, BigDecimal avgExpenses,
                        BigDecimal existingLoanRepayment, BigDecimal amountOfLoan, BigDecimal interestRate,
                        Integer months) {
}
