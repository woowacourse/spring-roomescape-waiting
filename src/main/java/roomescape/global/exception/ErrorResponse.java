package roomescape.global.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponse(
        HttpStatus httpStatus,
        String errorMessage,
        LocalDateTime timeStamp,
        String apiUrl,
        String traceId
) { }
