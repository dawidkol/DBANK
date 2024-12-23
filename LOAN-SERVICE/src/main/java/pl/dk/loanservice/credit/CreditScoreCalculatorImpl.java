package pl.dk.loanservice.credit;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
class CreditScoreCalculatorImpl implements CreditScoreCalculator {

    @Override
    public BigDecimal calculateCreditScore(BigDecimal avgIncome, BigDecimal avgExpenses, BigDecimal avgBalance, BigDecimal existingLoanRepayments) {
        BigDecimal netIncome = avgIncome.subtract(avgExpenses);

        BigDecimal score = BigDecimal.ZERO;

        if (netIncome.compareTo(BigDecimal.ZERO) > 0) {
            score = score.add(netIncome.multiply(BigDecimal.valueOf(0.5)));
        }

        score = score.add(avgBalance.multiply(BigDecimal.valueOf(0.3)));

        if (existingLoanRepayments.compareTo(BigDecimal.ZERO) > 0) {
            score = score.subtract(existingLoanRepayments.multiply(BigDecimal.valueOf(0.2)));
        }

        return score.max(BigDecimal.ZERO);
    }
}
