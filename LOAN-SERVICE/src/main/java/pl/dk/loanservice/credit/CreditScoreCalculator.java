package pl.dk.loanservice.credit;

import java.math.BigDecimal;

interface CreditScoreCalculator {

    BigDecimal calculateCreditScore(BigDecimal avgIncome, BigDecimal avgExpenses,
                                    BigDecimal avgBalance, BigDecimal existingLoanRepayments);
}
