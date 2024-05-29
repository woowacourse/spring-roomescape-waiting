package roomescape.domain.reservation;

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

    protected Schedule() {
    }

    public Schedule(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public boolean isSameTime(ReservationTime reservationTime) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Schedule that = (Schedule) o;
        return Objects.equals(date, that.date) && Objects.equals(time, that.time)
               && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
