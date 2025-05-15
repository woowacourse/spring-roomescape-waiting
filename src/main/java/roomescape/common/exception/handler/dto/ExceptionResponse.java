package roomescape.common.exception.handler.dto;

public record ExceptionResponse(int status, String message, String path) {
}
