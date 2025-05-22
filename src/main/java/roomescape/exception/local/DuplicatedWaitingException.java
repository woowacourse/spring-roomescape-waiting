package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class DuplicatedWaitingException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "중복된 대기 데이터입니다.";

    public DuplicatedWaitingException(String message) {
        super(message);
    }

    public DuplicatedWaitingException() {
        this(DEFAULT_MESSAGE);
    }
}
