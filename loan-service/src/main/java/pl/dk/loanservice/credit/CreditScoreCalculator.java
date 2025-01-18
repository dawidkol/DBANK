package pl.dk.loanservice.credit;

import java.math.BigDecimal;

public interface CreditScoreCalculator {

    BigDecimal calculateCreditScore(BigDecimal avgIncome, BigDecimal avgExpenses,
                                    BigDecimal avgBalance, BigDecimal existingLoanRepayments);
}
