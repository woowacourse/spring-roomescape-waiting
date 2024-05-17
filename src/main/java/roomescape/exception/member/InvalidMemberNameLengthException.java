package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class InvalidMemberNameLengthException extends RoomescapeException {
    public InvalidMemberNameLengthException() {
        super("이름은 2~5자 사이여야 합니다.", HttpStatus.BAD_REQUEST);
    }
}
