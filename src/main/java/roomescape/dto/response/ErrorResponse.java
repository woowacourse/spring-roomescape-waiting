package roomescape.dto.response;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ErrorResponse(String message, LocalDateTime timestamp) {

    public ErrorResponse(String message) {
        this(message, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }
}
