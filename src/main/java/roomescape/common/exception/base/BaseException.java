package roomescape.common.exception.base;

import org.springframework.http.HttpStatus;

import java.util.Optional;

public abstract class BaseException extends RuntimeException {

    private final String userMessage;
    private final boolean userVisible;

    protected BaseException(final String logMessage,
                            final String userMessage,
                            final Throwable cause) {
        super(logMessage, cause);
        this.userMessage = userMessage;
        this.userVisible = true;
    }

    protected BaseException(final String logMessage,
                            final Throwable cause) {
        super(logMessage, cause);
        this.userMessage = "";
        this.userVisible = false;
    }

    protected BaseException(final String logMessage,
                            final String userMessage) {
        super(logMessage);
        this.userMessage = userMessage;
        this.userVisible = true;
    }

    protected BaseException(final String logMessage) {
        super(logMessage);
        this.userMessage = "";
        this.userVisible = false;
    }

    public Optional<String> getUserMessage() {
        if (userVisible) {
            return Optional.of(userMessage);
        }
        return Optional.empty();
    }

    public abstract HttpStatus getHttpStatus();
}
