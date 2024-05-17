package roomescape.exception;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ErrorResponse {
    private final String message;

    @JsonCreator
    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
