package pl.dk.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class LoanServiceUnavailable extends RuntimeException {

    public LoanServiceUnavailable() {
        super("Loan Service unavailable, try again later");
    }
}
