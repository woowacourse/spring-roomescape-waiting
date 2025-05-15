package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class MemberNotFound extends BusinessException {
    public MemberNotFound(String message) {
        super(message);
    }
}
