package pl.dk.cardservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class FeignClientException extends RuntimeException {

    public FeignClientException(String msg) {
        super(msg);
    }
}
