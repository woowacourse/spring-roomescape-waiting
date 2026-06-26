package roomescape.controller.dto;

import java.time.LocalTime;

public record TimePatchRequest(
        LocalTime startAt
) {
}
