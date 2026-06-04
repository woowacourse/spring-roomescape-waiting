package roomescape.common.dto;

public record ErrorResponse(
        String message,
        String errorCode
) {
}
