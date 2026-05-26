package roomescape.service.result;

import java.time.LocalTime;
import roomescape.repository.dto.TimeSlotProjection;

public record ThemeTimesResult(
        Long id,
        LocalTime startAt,
        boolean isReservable
) {
    public static ThemeTimesResult from(TimeSlotProjection projection) {
        return new ThemeTimesResult(
                projection.id(),
                projection.startAt(),
                projection.isReservable()
        );
    }
}
