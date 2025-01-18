package pl.dk.accounts_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class UserServiceUnavailableException extends RuntimeException {

    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
