package roomescape.controller.client.api.dto;

import java.time.LocalTime;
import roomescape.service.result.ThemeTimesResult;

public record ThemeTimesResponse(
        long id,
        LocalTime startAt,
        boolean isReservable,
        String status
) {
    public static ThemeTimesResponse from(ThemeTimesResult result) {
        return new ThemeTimesResponse(result.id(), result.startAt(), result.isReservable(), result.status());
    }
}
