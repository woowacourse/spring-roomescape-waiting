package roomescape.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
