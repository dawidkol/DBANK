package pl.dk.loanservice.loan_details;

import pl.dk.loanservice.loan_details.dtos.LoanDetailsDto;

interface LoanDetailsService {

    LoanDetailsDto getLoanDetails(String loanId);
}
