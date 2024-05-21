package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class InvalidMemberEmailPatternException extends RoomescapeException {
    public InvalidMemberEmailPatternException() {
        super("유효하지 않은 이메일 형식입니다.", HttpStatus.BAD_REQUEST);
    }
}
