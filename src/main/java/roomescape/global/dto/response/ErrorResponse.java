package roomescape.global.dto.response;

import roomescape.global.constant.GlobalConstant;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ErrorResponse(String message, LocalDateTime timestamp) {

    public ErrorResponse(String message) {
        this(message, LocalDateTime.now(ZoneId.of(GlobalConstant.TIME_ZONE)));
    }
}
