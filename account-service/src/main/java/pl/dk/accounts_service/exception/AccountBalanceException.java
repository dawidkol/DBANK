package pl.dk.accounts_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AccountBalanceException extends RuntimeException{

    public AccountBalanceException(String message) {
        super(message);
    }
}
