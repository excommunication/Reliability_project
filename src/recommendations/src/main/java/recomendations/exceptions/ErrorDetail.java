package recomendations.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorDetail {
    private int code;
    private String message;
    private LocalDateTime time = LocalDateTime.now();

    public ErrorDetail(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
