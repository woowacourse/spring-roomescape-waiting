package roomescape.reservation.infrastructure.projection;

import roomescape.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationDetailProjection(
        Long id,
        Long memberId,
        String memberName,
        String memberPassword,
        Role memberRole,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailUrl,
        Long timeId,
        LocalTime startAt
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
