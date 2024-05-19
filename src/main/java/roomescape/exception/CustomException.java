package roomescape.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException {
    private final HttpStatus status;

    protected CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
