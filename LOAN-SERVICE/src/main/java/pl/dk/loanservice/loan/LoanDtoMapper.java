package pl.dk.loanservice.loan;

import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

class LoanDtoMapper {

    public static Loan map(CreateLoanDto createLoanDto) {
        return Loan.builder()
                .userId(createLoanDto.userId())
                .amount(createLoanDto.amount())
                .interestRate(createLoanDto.interestRate())
                .startDate(createLoanDto.startDate())
                .endDate(createLoanDto.endDate())
                .description(createLoanDto.description())
                .build();
    }

    public static LoanDto map(Loan savedLoan) {
        return LoanDto.builder()
                .id(savedLoan.getId())
                .userId(savedLoan.getUserId())
                .amount(savedLoan.getAmount())
                .interestRate(savedLoan.getInterestRate())
                .startDate(savedLoan.getStartDate())
                .endDate(savedLoan.getEndDate())
                .remainingAmount(savedLoan.getRemainingAmount())
                .status(savedLoan.getStatus().name())
                .description(savedLoan.getDescription())
                .build();
    }
}
