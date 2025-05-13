package roomescape.exception.member;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class MemberFieldRequiredException extends RoomescapeException {

    public MemberFieldRequiredException(String field) {
        super(HttpStatus.BAD_REQUEST, field + "은/는 필수 입력값입니다.");
    }
}
