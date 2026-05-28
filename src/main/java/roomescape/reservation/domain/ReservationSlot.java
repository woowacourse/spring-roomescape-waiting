package roomescape.reservation.domain;

import roomescape.date.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record ReservationSlot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
) {
    public static ReservationSlot of(ReservationDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
    }

    public Long getDateId() {
        return date.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

}
