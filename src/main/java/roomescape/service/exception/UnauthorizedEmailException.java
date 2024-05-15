package roomescape.service.exception;

import roomescape.exception.UnauthorizedException;

public class UnauthorizedEmailException extends UnauthorizedException {

    public UnauthorizedEmailException(String message) {
        super(message);
    }
}
