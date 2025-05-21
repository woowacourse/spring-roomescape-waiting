package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class InvalidMemberException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 유저입니다.";

    public InvalidMemberException(String message) {
        super(message);
    }

    public InvalidMemberException() {
        this(DEFAULT_MESSAGE);
    }
}
