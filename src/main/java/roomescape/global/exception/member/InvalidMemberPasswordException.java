package roomescape.global.exception.member;

import roomescape.global.exception.IllegalRequestException;

public class InvalidMemberPasswordException extends IllegalRequestException {

    public InvalidMemberPasswordException(String message) {
        super(message);
    }

    public InvalidMemberPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
