package roomescape.error;

public record ErrorResponse(ErrorCode code, String message) {
    public static ErrorResponse of(ErrorCode code) {
        return new ErrorResponse(code, code.getMessage());
    }
}
