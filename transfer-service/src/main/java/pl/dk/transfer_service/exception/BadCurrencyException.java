package pl.dk.transfer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
public class BadCurrencyException extends RuntimeException {

    public BadCurrencyException(String message) {
        super(message);
    }
}
