package roomescape.exception;

public record ErrorResponse(int status, String errorType, String message) {
}
