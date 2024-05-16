package roomescape.global.exception.member;

import roomescape.global.exception.IllegalRequestException;

public class InvalidMemberEmailException extends IllegalRequestException {

    public InvalidMemberEmailException(String message) {
        super(message);
    }

    public InvalidMemberEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
