package roomescape.infra.toss;

import roomescape.global.exception.ErrorCode;

public class RetryablePaymentException extends RuntimeException {

    private final ErrorCode errorCode;

    public RetryablePaymentException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
