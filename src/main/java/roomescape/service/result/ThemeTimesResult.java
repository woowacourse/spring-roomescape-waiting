package roomescape.service.result;

import java.time.LocalTime;
import roomescape.repository.dto.TimeSlotProjection;

public record ThemeTimesResult(
        Long id,
        LocalTime startAt,
        boolean isReservable,
        String status
) {
    public static ThemeTimesResult from(TimeSlotProjection projection) {
        return new ThemeTimesResult(
                projection.id(),
                projection.startAt(),
                projection.isReservable(),
                resolveStatus(projection.isReservable())
        );
    }

    private static String resolveStatus(boolean reservable) {
        if (reservable) {
            return "RESERVABLE";
        }
        return "WAITING_AVAILABLE";
    }

    public static ThemeTimesResult unavailable(TimeSlotProjection projection) {
        return new ThemeTimesResult(
                projection.id(),
                projection.startAt(),
                false,
                "UNAVAILABLE"
        );
    }
}
