package roomescape.member.exception.model;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.model.RoomEscapeException;

public class MemberNotFoundException extends RoomEscapeException {

    private static final String MEMBER_NOT_EXIST_MESSAGE = "해당하는 유저가 존재하지 않습니다.";

    public MemberNotFoundException() {
        super(MEMBER_NOT_EXIST_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
