package roomescape.service.exception;

import roomescape.exception.UnauthorizedException;

public class UnauthorizedPasswordException extends UnauthorizedException {

    public UnauthorizedPasswordException(String message) {
        super(message);
    }
}
