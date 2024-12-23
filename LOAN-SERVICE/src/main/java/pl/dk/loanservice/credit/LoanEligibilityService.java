package pl.dk.loanservice.credit;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

interface LoanEligibilityService {

    CompletableFuture<BigDecimal> calculateEligibility(String userId);

}
