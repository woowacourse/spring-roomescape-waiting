package roomescape.member.exception;

import roomescape.common.exception.NotFoundException;

public class MemberNotFoundException extends NotFoundException {

    public MemberNotFoundException(final String message) {
        super(message);
    }
}
