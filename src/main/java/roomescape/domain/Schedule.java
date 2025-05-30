package roomescape.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class Schedule {

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public Schedule() {

    }

    public Schedule(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
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
    public boolean equals(final Object o) {
        if (!(o instanceof Schedule schedule)) return false;
        return Objects.equals(date, schedule.date) && Objects.equals(time, schedule.time) && Objects.equals(theme, schedule.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
