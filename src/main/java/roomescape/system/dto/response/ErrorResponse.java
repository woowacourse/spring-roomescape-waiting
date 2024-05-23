package roomescape.system.dto.response;

import roomescape.system.exception.error.ErrorType;

public record ErrorResponse(ErrorType errorType, String message) {
    public static ErrorResponse of(final ErrorType errorType, final String message) {
        return new ErrorResponse(errorType, message);
    }
}
