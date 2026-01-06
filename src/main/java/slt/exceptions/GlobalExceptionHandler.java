package slt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponse> handle(final InvalidDateException ex) {
        final var message = ex.getMessage();
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(final NotFoundException ex) {
        final var message = ex.getMessage();
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handle(final ValidationException ex) {
        final var message = ex.getMessage();
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handle(final UnauthorizedException ex) {
        final var message = ex.getMessage();
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConnectivityException.class)
    public ResponseEntity<ErrorResponse> handle(final ConnectivityException ex) {
        final var message = ex.getMessage();
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.FAILED_DEPENDENCY);
    }
}
