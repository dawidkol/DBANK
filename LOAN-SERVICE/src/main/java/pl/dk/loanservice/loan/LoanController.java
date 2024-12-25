package pl.dk.loanservice.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Validated
class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanDto> createLoan(@Valid @RequestBody CreateLoanDto createLoanDto) {
        LoanDto loanDto = loanService.createLoan(createLoanDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(loanDto.id())
                .toUri();
        return ResponseEntity.created(uri).body(loanDto);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDto> getLoanById(@PathVariable String loanId) {
        LoanDto loanById = loanService.getLoanById(loanId);
        return ResponseEntity.ok(loanById);
    }

    @GetMapping("/monthly-installment")
    public ResponseEntity<BigDecimal> determineMonthlyInstallment(@Positive @NotNull @RequestParam BigDecimal amount,
                                                                  @Positive @NotNull @RequestParam BigDecimal interestRate,
                                                                  @Positive @NotNull @RequestParam Integer months) {
        BigDecimal result = loanService.calculateMonthlyInstallment(
                amount,
                interestRate,
                months);
        return ResponseEntity.ok(result);
    }

}
