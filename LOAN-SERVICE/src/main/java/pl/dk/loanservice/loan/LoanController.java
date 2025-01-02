package pl.dk.loanservice.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.loanservice.loan.dtos.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static pl.dk.loanservice.constants.PagingAndSorting.DEFAULT_PAGE;
import static pl.dk.loanservice.constants.PagingAndSorting.DEFAULT_SIZE;


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

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<LoanDto>> getAllUsersLoans(@PathVariable String userId,
                                                          @RequestParam(required = false, defaultValue = DEFAULT_PAGE) int page,
                                                          @RequestParam(required = false, defaultValue = DEFAULT_SIZE) int size) {
        List<LoanDto> allUsersLoans = loanService.getAllUsersLoans(userId, page, size);
        return ResponseEntity.ok(allUsersLoans);
    }

    @PostMapping("/pay")
    public ResponseEntity<TransferDto> payInstallment(@Valid @RequestBody CreateLoanInstallmentTransfer createLoanInstallmentTransfer) {
        TransferDto transferDto = loanService.payInstallment(createLoanInstallmentTransfer);
        return ResponseEntity.status(HttpStatus.CREATED).body(transferDto);
    }


}
