package pl.dk.loanservice.credit;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
class LoanEligibilityServiceImpl implements LoanEligibilityService {

    @Override
    public CompletableFuture<BigDecimal> calculateEligibility(String userId) {
        return null;
    }
}
