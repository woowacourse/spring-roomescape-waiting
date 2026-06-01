package roomescape.exception.response;

import roomescape.exception.ErrorCode;

public record ErrorResponse(
        String exceptionCode,
        String message
) {
    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public ErrorResponse(String exceptionCode, String message) {
        this.exceptionCode = exceptionCode;
        this.message = message;
    }
}
