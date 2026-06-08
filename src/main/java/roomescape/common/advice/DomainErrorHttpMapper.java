package roomescape.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.domain.exception.DomainErrorCode;

@Component
public class DomainErrorHttpMapper {

    public HttpStatus statusOf(DomainErrorCode code) {
        return switch (code) {
            case INVALID_INPUT, PAST_RESERVATION -> HttpStatus.BAD_REQUEST;
            case DUPLICATE_RESERVATION, REFERENTIAL_INTEGRITY -> HttpStatus.CONFLICT;
            case UNAUTHORIZED_RESERVATION -> HttpStatus.FORBIDDEN;
            case RESERVATION_NOT_FOUND, WAITLIST_NOT_FOUND, RESERVATION_TIME_NOT_FOUND, THEME_NOT_FOUND ->
                HttpStatus.NOT_FOUND;
        };
    }
}
