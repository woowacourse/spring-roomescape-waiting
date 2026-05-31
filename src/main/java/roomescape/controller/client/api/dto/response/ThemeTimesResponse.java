package roomescape.controller.client.api.dto.response;

import java.time.LocalTime;

public record ThemeTimesResponse(
        long id,
        LocalTime startAt,
        boolean isReservable,
        String status
) {
}
