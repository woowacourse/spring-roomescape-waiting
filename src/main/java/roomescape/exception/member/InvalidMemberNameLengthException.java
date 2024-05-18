package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class InvalidMemberNameLengthException extends RoomescapeException {
    public InvalidMemberNameLengthException() {
        super("유효하지 않은 이름 길이입니다. 이름은 2~5자여야 합니다.", HttpStatus.BAD_REQUEST);
    }
}
