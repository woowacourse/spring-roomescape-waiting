package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class NameException extends BusinessException {
    public NameException(String message) {
        super(message);
    }
}
