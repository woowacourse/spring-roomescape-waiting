package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

public class Schedule {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    protected Schedule() {
    }

    public Schedule(final LocalDate date, final ReservationTime time, final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public boolean isSameTime(final ReservationTime reservationTime) {
        return time.equals(reservationTime);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Schedule schedule = (Schedule) o;
        return Objects.equals(date, schedule.date) && Objects.equals(time.getId(), schedule.time.getId())
               && Objects.equals(theme.getId(), schedule.theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
