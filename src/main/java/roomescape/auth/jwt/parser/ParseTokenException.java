package roomescape.auth.jwt.parser;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.AuthException;

public class ParseTokenException extends AuthException {

    public ParseTokenException(final String logMessage) {
        super(logMessage, buildUserMessage());
    }

    public ParseTokenException(final String logMessage, final Throwable cause) {
        super(logMessage, buildUserMessage(), cause);
    }

    private static String buildUserMessage() {
        return "올바르지 않은 토큰입니다.";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
