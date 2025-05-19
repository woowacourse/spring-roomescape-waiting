package roomescape.global.dto;

public record ErrorResponse(String message) {

    public static ErrorResponse from(final Exception exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
