package roomescape.common.dto;

import java.util.List;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.ErrorInformation;

public record ErrorDetail(
    int status,
    HttpStatus error,
    String errorCode,
    String message,
    List<InvalidParam> invalidParams
) {

    public static ErrorDetail of(HttpStatus error, String errorCode, String message) {
        return new ErrorDetail(error.value(), error, errorCode, message, null);
    }

    public static ErrorDetail of(ErrorInformation errorInformation) {
        HttpStatus httpStatus = errorInformation.getHttpStatus();
        return new ErrorDetail(httpStatus.value(), httpStatus, errorInformation.getErrorCode(),
            errorInformation.getMessage(), null);
    }

    public static ErrorDetail of(ErrorInformation errorInformation,
        List<InvalidParam> invalidParams) {
        HttpStatus httpStatus = errorInformation.getHttpStatus();
        return new ErrorDetail(httpStatus.value(), httpStatus, errorInformation.getErrorCode(),
            errorInformation.getMessage(), invalidParams);
    }

}
