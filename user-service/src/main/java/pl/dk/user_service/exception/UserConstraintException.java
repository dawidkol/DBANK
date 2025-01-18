package pl.dk.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserConstraintException extends RuntimeException{

    public UserConstraintException(String message) {
        super(message);
    }
    
}
