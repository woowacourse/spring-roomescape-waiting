package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class MemberNotFoundException extends RoomescapeException {

    public MemberNotFoundException(Long memberId) {
        super(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다. id=" + memberId);

    }

    public MemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다.");

    }
}
