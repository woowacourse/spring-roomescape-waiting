package roomescape.dto.response;

public record ErrorResponse(
        String message,
        String errorCode
) {
}
