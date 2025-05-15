package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class PasswordException extends BusinessException {
    public PasswordException(String message) {
        super(message);
    }
}
