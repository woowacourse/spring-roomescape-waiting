package roomescape.domain.waiting;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Waiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Waiting of(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Waiting(id, name, date, time, theme);
    }

    public static Waiting of(String name, LocalDate date, ReservationTime time, Theme theme) {
        time.validateIfTimePast(date);
        return new Waiting(null, name, date, time, theme);
    }

    public Long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public ReservationSlot getSlot() {
        return ReservationSlot.of(date, time, theme);
    }

}
