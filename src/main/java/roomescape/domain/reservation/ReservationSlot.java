package roomescape.domain.reservation;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationSlot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
) {

    public static ReservationSlot of(ReservationDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
    }

    public static ReservationSlot from(Reservation reservation) {
        return of(reservation.getDate(), reservation.getTime(), reservation.getTheme());
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

    public boolean isClosedForReservation(Clock clock) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date.getPlayDay(), time.getStartAt());
        LocalDateTime deadline = reservationDateTime.minus(Duration.ofMinutes(10));
        LocalDateTime now = LocalDateTime.now(clock);
        return now.isEqual(deadline) || now.isAfter(deadline);
    }
}
