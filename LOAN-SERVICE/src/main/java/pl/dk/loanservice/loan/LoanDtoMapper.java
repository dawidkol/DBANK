package pl.dk.loanservice.loan;

import pl.dk.loanservice.enums.CurrencyType;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

import java.util.Currency;

class LoanDtoMapper {

    public static Loan map(CreateLoanDto createLoanDto) {
        CurrencyType currencyType = CurrencyType.valueOf(createLoanDto.currencyType().toUpperCase());
        return Loan.builder()
                .userId(createLoanDto.userId())
                .amount(createLoanDto.amount())
                .interestRate(createLoanDto.interestRate())
                .startDate(createLoanDto.startDate())
                .endDate(createLoanDto.endDate())
                .description(createLoanDto.description())
                .currencyType(currencyType)
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
                .currencyType(savedLoan.getCurrencyType().name())
                .build();
    }
}
