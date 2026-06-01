package roomescape.domain.slot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Slot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Slot create(LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(null, date, time, theme);
    }

    public static Slot restore(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, date, time, theme);
    }

    public Slot withId(Long id) {
        return new Slot(id, this.date, this.time, this.theme);
    }

    public boolean isExpired() {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now());
    }

    public Long getId() {
        return id;
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
