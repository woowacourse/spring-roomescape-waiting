package roomescape.domain.reservation.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final LocalDateTime canceledAt;
    private final LocalDateTime deletedAt;

    private Reservation(Long id, String name, LocalDate date, Time time, Theme theme, LocalDateTime canceledAt,
        LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.canceledAt = canceledAt;
        this.deletedAt = deletedAt;
    }

    public static Reservation create(String name, LocalDate date, Time time, Theme theme) {
        return new Reservation(null, name, date, time, theme, null, null);
    }

    public static Reservation reconstruct(Long id, String name, LocalDate date, Time time, Theme theme,
        LocalDateTime canceledAt, LocalDateTime deletedAt) {
        return new Reservation(id, name, date, time, theme, canceledAt, deletedAt);
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

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
