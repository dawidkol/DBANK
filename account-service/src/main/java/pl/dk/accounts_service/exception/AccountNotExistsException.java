package pl.dk.accounts_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AccountNotExistsException extends RuntimeException {

    public AccountNotExistsException(String message) {
        super(message);
    }
}
