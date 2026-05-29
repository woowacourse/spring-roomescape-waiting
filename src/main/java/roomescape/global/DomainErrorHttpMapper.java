package roomescape.global;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.domain.exception.DomainErrorCode;

@Component
public class DomainErrorHttpMapper {

    public HttpStatus statusOf(DomainErrorCode code) {
        return switch (code) {
            case INVALID_INPUT, PAST_RESERVATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND_RESERVATION -> HttpStatus.NOT_FOUND;
            case DUPLICATE_RESERVATION, DUPLICATE_RESERVATION_TIME,
                 DUPLICATE_THEME_NAME, REFERENTIAL_INTEGRITY -> HttpStatus.CONFLICT;
        };
    }
}
