package roomescape.exception.local;

import roomescape.exception.global.NotFoundException;

public class NotFoundMemberException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 유저를 찾을 수 없습니다.";

    public NotFoundMemberException(String message) {
        super(message);
    }

    public NotFoundMemberException() {
        this(DEFAULT_MESSAGE);
    }
}
