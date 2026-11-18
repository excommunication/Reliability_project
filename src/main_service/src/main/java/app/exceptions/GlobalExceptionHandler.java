package app.exceptions;

import app.exceptions.WebExceptions.BadRequestException;
import app.exceptions.WebExceptions.InternalErrorException;
import app.exceptions.WebExceptions.ResourceNotFoundException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleNotFoud(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDetail(HttpStatus.NOT_FOUND.value(), e.getMessage())
                );
    }

    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<ErrorDetail> handleInternalError(InternalErrorException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage())
                );
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ErrorDetail> handleInternalError(SocketTimeoutException e) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorDetail(HttpStatus.GATEWAY_TIMEOUT.value(), e.getMessage())
                );
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorDetail> handleRateLimit(RequestNotPermitted e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorDetail(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. Try later.")
                );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetail> badRequestHandler(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetail(HttpStatus.BAD_REQUEST.value(), "Bad request")
                );
    }
}
