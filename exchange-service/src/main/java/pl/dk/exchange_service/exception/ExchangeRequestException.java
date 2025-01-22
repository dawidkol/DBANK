package pl.dk.exchange_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ExchangeRequestException extends RuntimeException{

    public ExchangeRequestException(String message) {
        super(message);
    }
}
