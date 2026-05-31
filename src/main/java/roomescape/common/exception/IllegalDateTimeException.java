package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class IllegalDateTimeException extends CustomException {

    public IllegalDateTimeException(String message) {
        super("ILLEGAL_DATE_TIME", HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
