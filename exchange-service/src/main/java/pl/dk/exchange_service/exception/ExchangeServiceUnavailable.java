package pl.dk.exchange_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ExchangeServiceUnavailable extends RuntimeException {

    public ExchangeServiceUnavailable(String message) {
        super(message);
    }
}
