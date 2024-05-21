package roomescape.domain.schedule;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
public class Schedule {
    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    protected Schedule() {
    }

    public Schedule(ReservationDate date, ReservationTime time) {
        this.date = date;
        this.time = time;
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public LocalTime getTime() {
        return time.getStartAt();
    }

    public ReservationDate getReservationDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }
}
