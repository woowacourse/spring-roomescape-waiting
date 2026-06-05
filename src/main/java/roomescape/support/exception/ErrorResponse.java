package roomescape.support.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

public record ErrorResponse(
    String code,
    String message,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String action
) {

    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).
            body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errorCode.getAction()));
    }

    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode, String customMessage) {
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(new ErrorResponse(errorCode.getCode(), customMessage, errorCode.getAction()));
    }

}
