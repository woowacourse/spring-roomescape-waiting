package roomescape.global.exception.member;

import roomescape.global.exception.IllegalRequestException;

public class InvalidMemberNameException extends IllegalRequestException {

    public InvalidMemberNameException(String message) {
        super(message);
    }

    public InvalidMemberNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
