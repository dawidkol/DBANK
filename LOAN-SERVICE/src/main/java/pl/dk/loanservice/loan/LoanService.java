package pl.dk.loanservice.loan;

import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;
import pl.dk.loanservice.loan.dtos.LoanEvent;

import java.math.BigDecimal;

interface LoanService {

    LoanDto createLoan(CreateLoanDto createLoanDto);

    LoanDto getLoanById(String loanId);

    BigDecimal calculateMonthlyInstallment(BigDecimal amountOfLoan, BigDecimal interestRate, Integer months);

    void evaluateCreditWorthiness(LoanEvent loanEvent);

}