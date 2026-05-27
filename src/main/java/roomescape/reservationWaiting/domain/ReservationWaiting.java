package roomescape.reservationWaiting.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationWaiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationWaiting of (String name, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationWaiting(null, name, date, time, theme);
    }

    public ReservationWaiting updateId(Long id) {
        return new ReservationWaiting(
                id,
                this.name,
                this.date,
                this.time,
                this.theme
        );
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
