package roomescape.global.exception.response;

import roomescape.global.exception.BusinessException;

public record ErrorResponse(String message) {
    public static ErrorResponse of(BusinessException e) {
        return new ErrorResponse(e.getMessage());
    }
}
