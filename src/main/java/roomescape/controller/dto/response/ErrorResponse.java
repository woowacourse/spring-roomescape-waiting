package roomescape.controller.dto.response;

import roomescape.exception.ErrorCode;

public record ErrorResponse(
        String code,
        String detail
) {

    public static ErrorResponse from(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode.name(), detail);
    }
}
