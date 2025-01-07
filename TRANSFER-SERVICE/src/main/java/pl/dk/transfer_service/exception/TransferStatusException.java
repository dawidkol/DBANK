package pl.dk.transfer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TransferStatusException extends RuntimeException {

    public TransferStatusException(String msg) {
        super(msg);
    }
}
