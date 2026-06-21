package roomescape.exception.code;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public enum RateLimitErrorCode implements ErrorCode {

    OUTBOUND_RATE_LIMIT_EXCEEDED(
            HttpStatus.SERVICE_UNAVAILABLE,
            "외부 결제 API 호출 한도를 초과했습니다. 잠시 후 다시 시도해 주세요."
    );

    private final HttpStatus httpStatus;
    private final String message;

    RateLimitErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
