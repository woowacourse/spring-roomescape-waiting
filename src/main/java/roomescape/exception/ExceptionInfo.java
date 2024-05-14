package roomescape.exception;

public class ExceptionInfo {
    private final String message;

    public ExceptionInfo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
