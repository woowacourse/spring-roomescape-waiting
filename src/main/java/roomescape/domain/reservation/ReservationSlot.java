package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationSlot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
) {

    public static ReservationSlot from(Reservation reservation) {
        return new ReservationSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    public Long dateId() {
        return date.getId();
    }

    public Long timeId() {
        return time.getId();
    }

    public Long themeId() {
        return theme.getId();
    }

    public boolean isSameSlot(ReservationSlot other) {
        return dateId().equals(other.dateId())
                && timeId().equals(other.timeId())
                && themeId().equals(other.themeId());
    }

    public boolean isOnOrBeforeToday(Clock clock) {
        return !date.getPlayDay().isAfter(LocalDate.now(clock));
    }
}
