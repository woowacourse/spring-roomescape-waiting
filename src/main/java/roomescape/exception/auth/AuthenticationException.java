package roomescape.exception.auth;

import roomescape.exception.RootSecurityException;
import roomescape.exception.SecurityErrorCode;

public class AuthenticationException extends RootSecurityException {

    public AuthenticationException(final SecurityErrorCode code) {
        super(code);
    }
}
