package games.exceptions;

import java.time.LocalDateTime;

public class ErrorDetail {
    private String message;
    private LocalDateTime time = LocalDateTime.now();
    private int statusCode;

    public ErrorDetail(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
