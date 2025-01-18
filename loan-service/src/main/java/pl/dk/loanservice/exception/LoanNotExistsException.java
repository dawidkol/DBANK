package pl.dk.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class LoanNotExistsException extends RuntimeException {

    public LoanNotExistsException(String msg) {
        super(msg);
    }
}
