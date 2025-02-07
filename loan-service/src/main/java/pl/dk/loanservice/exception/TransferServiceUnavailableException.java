package pl.dk.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class TransferServiceUnavailableException extends RuntimeException {

    public TransferServiceUnavailableException(String message) {
        super(message);
    }
}
