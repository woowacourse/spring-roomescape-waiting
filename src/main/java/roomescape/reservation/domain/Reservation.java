package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;

public class Reservation {
    private final Long id;
    private final String name;
    private final ReservationTime time;
    private final Long themeId;
    private final Theme theme;

    public Reservation(String name, ReservationTime time, Long themeId) {
        this(null, name, time, themeId, null);
    }

    private Reservation(Long id, String name, ReservationTime time, Long themeId, Theme theme) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.themeId = themeId;
        this.theme = theme;
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.time, this.themeId, this.theme);
    }

    public Reservation withTheme(Theme theme) {
        return new Reservation(this.id, this.name, this.time, this.themeId, theme);
    }

    public Reservation withTime(ReservationTime time) {
        return new Reservation(this.id, this.name, time, this.themeId, this.theme);
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

    public Long getThemeId() {
        return themeId;
    }

    public Theme getTheme() {
        return theme;
    }
}
