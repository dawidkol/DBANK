package pl.dk.exchange_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ServerException extends RuntimeException {

    public ServerException(String message) {
        super(message);
    }
}
