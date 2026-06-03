package roomescape.exception;

import org.springframework.http.HttpStatus;
import roomescape.domain.exception.DomainErrorCode;

public class DomainErrorHttpMapper {

    private DomainErrorHttpMapper() {

    }

    public static HttpStatus statusOf(DomainErrorCode code) {
        return switch (code) {
            case NOT_FOUND_RESERVATION, NOT_FOUND_WAIT, NOT_FOUND_RESERVATION_TIME, NOT_FOUND_THEME ->
                    HttpStatus.NOT_FOUND;
            case DUPLICATED_RESERVATION, DUPLICATED_WAIT, DUPLICATED_RESERVATION_TIME,
                 REFERENCED_TIME, REFERENCED_THEME, WAIT_IS_FULL -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
