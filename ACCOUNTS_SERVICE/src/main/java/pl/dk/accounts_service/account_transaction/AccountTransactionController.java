package pl.dk.accounts_service.account_transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
class AccountTransactionController {

    private final AccountTransactionService accountTransactionService;

    @GetMapping("/last-12-months/{userId}")
    public ResponseEntity<BigDecimal> getAvgLast12Moths(@PathVariable String userId) {
        BigDecimal average = accountTransactionService.getAverageBalanceFromDeclaredMonths(userId, 12);
        return ResponseEntity.ok(average);
    }
}
