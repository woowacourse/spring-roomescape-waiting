package roomescape.common.exception.base;

public abstract class AuthException extends BaseException {

    protected AuthException(final String logMessage,
                            final String userMessage,
                            final Throwable cause) {
        super(logMessage, userMessage, cause);
    }

    protected AuthException(final String logMessage,
                            final Throwable cause) {
        super(logMessage, cause);
    }

    protected AuthException(final String logMessage,
                            final String userMessage) {
        super(logMessage, userMessage);
    }

    protected AuthException(final String logMessage) {
        super(logMessage);
    }
}
