package roomescape.auth.exception;

import roomescape.global.exception.BusinessException;

public class MissingAuthorizationHeaderException extends BusinessException {

    public MissingAuthorizationHeaderException() {
        super("Authorization 헤더가 필요합니다.");
    }
}
