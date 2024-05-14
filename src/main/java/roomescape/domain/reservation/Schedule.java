package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public class Schedule {
    private final ReservationDate date;
    private final ReservationTime time;

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

    public ReservationTime getReservationTime() {
        return time;
    }
}
