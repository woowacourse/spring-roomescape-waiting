package common.exception;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final String message;

    public static ErrorResponse create(String message) {
        return new ErrorResponse(LocalDateTime.now(), message);
    }
}
