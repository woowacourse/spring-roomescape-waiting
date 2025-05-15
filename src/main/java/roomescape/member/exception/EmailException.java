package roomescape.member.exception;

import roomescape.common.exception.BusinessException;

public class EmailException extends BusinessException {
    public EmailException(String message) {
        super(message);
    }
}
