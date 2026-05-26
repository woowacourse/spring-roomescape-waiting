package roomescape.controller.dto;

import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

public record ErrorResponse(
        String code,
        String detail
) {

    public static ErrorResponse from(RoomescapeException exception) {
        return new ErrorResponse(exception.getErrorCode().name(), exception.getMessage());
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return from(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse from(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode.name(), detail);
    }
}
