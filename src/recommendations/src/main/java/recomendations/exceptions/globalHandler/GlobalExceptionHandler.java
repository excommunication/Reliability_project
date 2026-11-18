package recomendations.exceptions.globalHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import recomendations.exceptions.ErrorDetail;
import recomendations.exceptions.InternalErrorException;
import recomendations.exceptions.BadRequestException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetail> handleNotFoud(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDetail(HttpStatus.BAD_REQUEST.value(), e.getMessage())
                );
    }

    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<ErrorDetail> handleInternalError(InternalErrorException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage())
                );
    }
}