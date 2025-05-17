package roomescape.member.exception;

import roomescape.global.common.exception.ValidationException;

public class InvalidMemberException extends ValidationException {

    public InvalidMemberException(final String message) {
        super(message);
    }
}
