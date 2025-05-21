package roomescape.member.exception;

import roomescape.common.exception.DuplicatedException;

public class MemberDuplicatedException extends DuplicatedException {

    public MemberDuplicatedException(final String message) {
        super(message);
    }
}
