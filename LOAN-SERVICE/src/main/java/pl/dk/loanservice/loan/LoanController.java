package pl.dk.loanservice.loan;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
class LoanController {

    private final LoanService loanService;


    @PostMapping
    public ResponseEntity<?> createLoan(@Valid @RequestBody CreateLoanDto createLoanDto) {
        LoanDto loan = loanService.createLoan(createLoanDto);
        return ResponseEntity.ok(loan);
    }
}
