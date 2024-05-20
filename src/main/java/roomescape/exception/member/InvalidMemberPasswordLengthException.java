package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class InvalidMemberPasswordLengthException extends RoomescapeException {
    public InvalidMemberPasswordLengthException() {
        super("유효하지 않은 비밀번호 길이입니다. 비밀번호는 8~16자여야 합니다.", HttpStatus.BAD_REQUEST);
    }
}
