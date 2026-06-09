package roomescape.common.exception;

import org.springframework.http.HttpStatus;
import roomescape.domain.DomainErrorCode;

public enum ExceptionType {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, DomainErrorCode.RESOURCE_NOT_FOUND),
    ALREADY_EXISTS(HttpStatus.CONFLICT, DomainErrorCode.ALREADY_EXISTS),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, DomainErrorCode.INVALID_INPUT),
    PAST_DATE(HttpStatus.UNPROCESSABLE_ENTITY, DomainErrorCode.PAST_DATE),
    FORBIDDEN(HttpStatus.UNAUTHORIZED, DomainErrorCode.FORBIDDEN),
    RESOURCE_IN_USE(HttpStatus.CONFLICT, DomainErrorCode.RESOURCE_IN_USE);

    private final HttpStatus status;
    private final DomainErrorCode domainErrorCode;

    ExceptionType(HttpStatus status, DomainErrorCode domainErrorCode) {
        this.status = status;
        this.domainErrorCode = domainErrorCode;
    }

    public static HttpStatus resolveStatus(DomainErrorCode code) {
        return switch (code) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case PAST_DATE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_IN_USE -> HttpStatus.CONFLICT;
        };
    }

    public HttpStatus getStatus() {
        return status;
    }
}
