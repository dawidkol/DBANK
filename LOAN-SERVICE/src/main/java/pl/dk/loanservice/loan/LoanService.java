package pl.dk.loanservice.loan;

import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

interface LoanService {

    LoanDto createLoan(CreateLoanDto createLoanDto);

    LoanDto getLoanById(String loanId);
}