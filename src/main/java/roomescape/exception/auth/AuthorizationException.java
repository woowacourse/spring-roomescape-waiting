package roomescape.exception.auth;

import roomescape.exception.RootSecurityException;
import roomescape.exception.SecurityErrorCode;

public class AuthorizationException extends RootSecurityException {
    
    public AuthorizationException(final SecurityErrorCode code) {
        super(code);
    }
}
