package roomescape.domain.reservation.entity;

import java.time.LocalDate;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Status status;

    private Reservation(Long id, String name, LocalDate date, Time time, Theme theme, Status status) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation create(String name, LocalDate date, Time time, Theme theme) {
        return new Reservation(null, name, date, time, theme, Status.ACTIVE);
    }

    public static Reservation reconstruct(Long id, String name, LocalDate date, Time time, Theme theme, Status status) {
        return new Reservation(id, name, date, time, theme, status);
    }

    public Reservation cancel() {
        return new Reservation(this.id, this.name, this.date, this.time, this.theme, Status.CANCELED);
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

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }
}
