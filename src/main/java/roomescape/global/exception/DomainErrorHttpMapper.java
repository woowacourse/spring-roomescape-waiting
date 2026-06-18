package roomescape.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.domain.exception.DomainErrorCode;

@Component
public class DomainErrorHttpMapper {

    public HttpStatus statusOf(DomainErrorCode code) {
        return switch (code) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case INVALID_LOGIN,
                 UNAUTHENTICATED
                    -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED_ADMIN,
                 UNAUTHORIZED_RESERVATION
                    -> HttpStatus.FORBIDDEN;
            case NOT_FOUND_RESERVATION,
                 NOT_FOUND_RESERVATION_TIME,
                 NOT_FOUND_THEME,
                 NOT_FOUND_SCHEDULE,
                 NOT_FOUND_PAYMENT_ORDER,
                 PAYMENT_NOT_FOUND
                    -> HttpStatus.NOT_FOUND;
            case PAYMENT_AUTHENTICATION_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
            case PAYMENT_CONFIRM_UNKNOWN -> HttpStatus.ACCEPTED;
            case PAYMENT_RETRYABLE,
                 PAYMENT_GATEWAY_ERROR
                    -> HttpStatus.BAD_GATEWAY;
            case PAST_RESERVATION,
                 REFERENTIAL_INTEGRITY,
                 PAYMENT_AMOUNT_MISMATCH,
                 PAYMENT_SLOT_UNAVAILABLE,
                 PAYMENT_INVALID_REQUEST,
                 PAYMENT_REJECTED
                    -> HttpStatus.UNPROCESSABLE_ENTITY;
            case DUPLICATE_RESERVATION,
                 DUPLICATE_RESERVATION_TIME,
                 DUPLICATE_THEME_NAME,
                 DUPLICATE_MEMBER,
                 PAYMENT_ALREADY_PROCESSED
                    -> HttpStatus.CONFLICT;
        };
    }
}
