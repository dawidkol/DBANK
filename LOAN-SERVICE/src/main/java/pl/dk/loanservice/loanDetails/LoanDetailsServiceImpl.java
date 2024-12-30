package pl.dk.loanservice.loanDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.dk.loanservice.exception.AccountNotExistsException;
import pl.dk.loanservice.exception.LoanDetailsNotExistsException;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.AccountDto;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.loanDetails.dtos.LoanDetailsDto;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
class LoanDetailsServiceImpl implements LoanDetailsService {

    private final LoanRepository loanRepository;
    private final LoanDetailsRepository loanDetailsRepository;
    private final AccountServiceFeignClient accountServiceFeignClient;

    @Override
    public LoanDetailsDto getLoanDetails(String loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() ->
                new LoanNotExistsException("Loan with id %s not found".formatted(loanId)));
        LoanDetails loanDetails = loanDetailsRepository.findByLoan_id(loanId).orElseThrow(() ->
                new LoanDetailsNotExistsException("Loan with id %s not found".formatted(loanId)));

        String loanAccountNumber = loanDetails.getLoanAccountNumber();
        ResponseEntity<AccountDto> accountById = accountServiceFeignClient.getAccountById(loanAccountNumber);
        if (accountById.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            throw new AccountNotExistsException("Account with number: %s not found".formatted(loanAccountNumber));
        }

        AccountDto accountDto = accountById.getBody();
        assert accountDto != null;
        BigDecimal amountPaid = accountDto.balance();
        BigDecimal loanAmount = loan.getAmount();
        return LoanDetailsDto.builder()
                .loanId(loan.getId())
                .userId(loan.getUserId())
                .amount(loanAmount)
                .remainingAmount(loanAmount.subtract(amountPaid))
                .amountPaid(amountPaid)
                .loanAccount(loanAccountNumber)
                .lastPaymentDateTo(loan.getEndDate())
                .build();
    }
}
