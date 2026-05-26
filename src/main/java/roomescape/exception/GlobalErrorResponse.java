package roomescape.exception;

public class GlobalErrorResponse {
    private final String message;

    public GlobalErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
