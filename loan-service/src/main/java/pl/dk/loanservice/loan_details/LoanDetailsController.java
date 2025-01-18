package pl.dk.loanservice.loan_details;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.dk.loanservice.loan_details.dtos.LoanDetailsDto;

@RestController
@RequestMapping("/loan-details")
@RequiredArgsConstructor
class LoanDetailsController {

    private final LoanDetailsService loanDetailsService;

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDetailsDto> getLoanDetailsByLoanId(@PathVariable String loanId) {
        LoanDetailsDto loanDetails = loanDetailsService.getLoanDetails(loanId);
        return ResponseEntity.ok(loanDetails);
    }

}
