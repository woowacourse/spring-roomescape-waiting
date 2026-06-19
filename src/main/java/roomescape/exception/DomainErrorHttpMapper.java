package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.domain.exception.DomainErrorCode;

@Component
public class DomainErrorHttpMapper {

    public HttpStatus statusOf(DomainErrorCode code) {
        return switch (code) {
            case NOT_FOUND_RESERVATION, NOT_FOUND_WAIT, NOT_FOUND_RESERVATION_TIME, NOT_FOUND_THEME,
                 NOT_FOUND_PAYMENT_ORDER, NOT_FOUND_PAYMENT ->
                    HttpStatus.NOT_FOUND;
            case DUPLICATED_RESERVATION, DUPLICATED_WAIT, DUPLICATED_RESERVATION_TIME,
                 REFERENCED_TIME, REFERENCED_THEME, WAIT_IS_FULL, SLOT_JUST_TAKEN,
                 PAYMENT_ALREADY_PROCESSED, DUPLICATED_PAYMENT_ORDER -> HttpStatus.CONFLICT;
            case PAYMENT_GATEWAY_CONFIG_ERROR -> HttpStatus.BAD_GATEWAY;
            case PAYMENT_RETRYABLE, PAYMENT_UNKNOWN -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
