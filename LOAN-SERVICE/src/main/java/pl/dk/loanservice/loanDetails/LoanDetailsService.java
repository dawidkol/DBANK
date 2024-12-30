package pl.dk.loanservice.loanDetails;

import pl.dk.loanservice.loanDetails.dtos.LoanDetailsDto;

interface LoanDetailsService {

    LoanDetailsDto getLoanDetails(String loanId);
}
