package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class IllegalDateTimeException extends CustomException {

    public IllegalDateTimeException(final String message) {
        super("ILLEGAL_DATE_TIME", HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
