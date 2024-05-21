package roomescape.exception;


import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException {
    private final HttpStatus status;

    protected CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
