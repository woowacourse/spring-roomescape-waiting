package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class NotFoundMemberException extends RoomescapeException {
    public NotFoundMemberException() {
        super("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
    }
}
