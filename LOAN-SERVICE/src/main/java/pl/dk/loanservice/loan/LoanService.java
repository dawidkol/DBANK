package pl.dk.loanservice.loan;

import pl.dk.loanservice.loan.dtos.*;

import java.math.BigDecimal;
import java.util.List;

interface LoanService {

    LoanDto createLoan(CreateLoanDto createLoanDto);

    LoanDto getLoanById(String loanId);

    BigDecimal calculateMonthlyInstallment(BigDecimal amountOfLoan, BigDecimal interestRate, Integer months);

    void evaluateCreditWorthiness(LoanEvent loanEvent);

    List<LoanDto> getAllUsersLoans(String userId, int page, int size);

//    CreateLoanAccountDto createLoanAccount(String userId, String loanId);

    TransferDto payInstallment(String loanId, CreateTransferDto createPayInstallmentDto);

}