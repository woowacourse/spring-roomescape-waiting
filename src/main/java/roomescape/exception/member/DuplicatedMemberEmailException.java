package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class DuplicatedMemberEmailException extends RoomescapeException {
    public DuplicatedMemberEmailException() {
        super("해당 이메일이 이미 존재합니다.", HttpStatus.CONFLICT);
    }
}
