package roomescape.common.exception.base;

public abstract class BusinessException extends BaseException {

    protected BusinessException(final String message,
                                final String userMessage,
                                final Throwable cause) {
        super(message, userMessage, cause);
    }

    protected BusinessException(final String message,
                                final Throwable cause) {
        super(message, cause);
    }

    protected BusinessException(final String message,
                                final String userMessage) {
        super(message, userMessage);
    }

    protected BusinessException(final String message) {
        super(message);
    }
}
