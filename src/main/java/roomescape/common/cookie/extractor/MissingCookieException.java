package roomescape.common.cookie.extractor;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.AuthException;

public class MissingCookieException extends AuthException {

    public MissingCookieException(final String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
