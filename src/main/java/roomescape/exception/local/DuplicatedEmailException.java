package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class DuplicatedEmailException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "중복된 이메일입니다.";

    public DuplicatedEmailException(String message) {
        super(message);
    }

    public DuplicatedEmailException() {
        this(DEFAULT_MESSAGE);
    }
}
