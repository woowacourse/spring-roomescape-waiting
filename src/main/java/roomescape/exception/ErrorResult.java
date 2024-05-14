package roomescape.exception;

public class ErrorResult {

    private final String message;

    public ErrorResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
