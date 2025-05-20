package roomescape.auth.sign.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.AuthException;
import roomescape.common.exception.util.ExceptionMessageFormatter;

public class InvalidSignInException extends AuthException {

    public InvalidSignInException(final Object... params) {
        super(
                buildLogMessage(params),
                buildUserMessage()
        );
    }

    private static String buildLogMessage(final Object... params) {
        return ExceptionMessageFormatter.format("Password mismatch", params);
    }

    private static String buildUserMessage() {
        return "아이디 혹은 비밀번호가 잘못됐습니다.";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
