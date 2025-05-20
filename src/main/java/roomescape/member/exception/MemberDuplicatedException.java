package roomescape.member.exception;

import roomescape.exception.DuplicatedException;

public class MemberDuplicatedException extends DuplicatedException {

    public MemberDuplicatedException(final String message) {
        super(message);
    }
}
