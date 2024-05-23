package roomescape.handler.dto;

import org.springframework.http.HttpStatus;

public record ExceptionResponse(HttpStatus error, String message) {
}
