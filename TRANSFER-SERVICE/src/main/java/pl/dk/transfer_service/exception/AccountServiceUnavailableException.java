package pl.dk.transfer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class AccountServiceUnavailableException extends RuntimeException {

    public AccountServiceUnavailableException(String message) {
        super(message);
    }
}
