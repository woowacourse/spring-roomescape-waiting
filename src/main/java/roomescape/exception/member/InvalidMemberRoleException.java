package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class InvalidMemberRoleException extends RoomescapeException {
    public InvalidMemberRoleException() {
        super("존재하지 않는 역할입니다.", HttpStatus.BAD_REQUEST);
    }
}
