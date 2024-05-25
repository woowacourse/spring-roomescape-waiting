package roomescape.domain.reservation;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ReservationSlot {
    @Temporal(TemporalType.DATE)
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    protected ReservationSlot() {}

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationSlot that = (ReservationSlot) o;
        return Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
