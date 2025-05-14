package roomescape.global.exception;

public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
