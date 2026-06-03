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
                 NOT_FOUND_SCHEDULE
                    -> HttpStatus.NOT_FOUND;
            case PAST_RESERVATION, REFERENTIAL_INTEGRITY -> HttpStatus.UNPROCESSABLE_ENTITY;
            case DUPLICATE_RESERVATION,
                 DUPLICATE_RESERVATION_TIME,
                 DUPLICATE_THEME_NAME,
                 DUPLICATE_MEMBER
                    -> HttpStatus.CONFLICT;
        };
    }
}
