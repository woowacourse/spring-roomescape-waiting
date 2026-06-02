package roomescape.reservationwaiting.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationWaiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationWaiting restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationWaiting(id, name, date, time, theme);
    }

    public boolean isPast(Clock clock) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now(clock));
    }

    public boolean isCancelable(Clock clock) {
        return LocalDateTime.now(clock).isBefore(LocalDateTime.of(date, time.getStartAt()).minusHours(12));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
