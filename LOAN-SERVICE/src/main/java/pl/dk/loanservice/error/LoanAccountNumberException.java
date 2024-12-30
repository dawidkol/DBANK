package pl.dk.loanservice.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class LoanAccountNumberException extends RuntimeException{

    public LoanAccountNumberException(String msg) {
        super(msg);
    }
}
