package roomescape.domain.reservation;

import java.time.LocalDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class ReservationSlot {

    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot of(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}