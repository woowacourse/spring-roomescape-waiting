package roomescape.global.exception;

public record ValidationError(String field, String reason) {
}
