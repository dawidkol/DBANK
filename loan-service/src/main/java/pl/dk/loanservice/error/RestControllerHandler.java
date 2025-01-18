package pl.dk.loanservice.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RestControllerHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public List<MethodArgumentNotValidExceptionWrapper> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(er -> new MethodArgumentNotValidExceptionWrapper(er.getField(),
                        er.getDefaultMessage()))
                .toList();
    }

    public record MethodArgumentNotValidExceptionWrapper(String property, String message) { }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public List<ConstraintViolationExceptionWrapper> handleConstraintViolationException(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(constraintViolation ->
                        new ConstraintViolationExceptionWrapper(constraintViolation.getInvalidValue().toString(),
                                constraintViolation.getMessage()))
                .toList();
    }

    public record ConstraintViolationExceptionWrapper(String value, String message) { }

}
