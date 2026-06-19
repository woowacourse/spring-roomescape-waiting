package roomescape.reservation.application.port.out.projection;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationDetailProjection(
        Long id,
        Long memberId,
        String memberName,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailUrl,
        int themePrice,
        Long timeId,
        LocalTime startAt,
        ReservationStatus status
) {
    public long getTimeId() {
        return timeId();
    }

    public long getThemeId() {
        return themeId();
    }

    public LocalTime getTime() {
        return startAt();
    }
}
