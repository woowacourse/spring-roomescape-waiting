package roomescape.reservation.domain.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.CustomException;

public class PastDateTimeException extends CustomException {
    public PastDateTimeException(String message) {
        super("PAST_TIME", HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
