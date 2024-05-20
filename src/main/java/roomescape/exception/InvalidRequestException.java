package roomescape.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends RoomescapeException {

    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
