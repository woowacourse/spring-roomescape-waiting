package roomescape.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
abstract class CustomException extends RuntimeException {

    private final HttpStatus status;

    CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
