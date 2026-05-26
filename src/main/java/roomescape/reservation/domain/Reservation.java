package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class Reservation {
    private final Long id;
    private final String name;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(String name, ReservationTime time) {
        this(null, name, time, null);
    }

    private Reservation(Long id, String name, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.theme = theme;
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.time, this.theme);
    }

    public Reservation withTheme(Theme theme) {
        return new Reservation(this.id, this.name, this.time, theme);
    }

    public Reservation withTime(ReservationTime time) {
        return new Reservation(this.id, this.name, time, this.theme);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
