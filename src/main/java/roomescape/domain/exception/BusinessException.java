package roomescape.domain.exception;

import lombok.Getter;

public final class BusinessException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }
}
