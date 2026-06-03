package roomescape.time;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationTime {
    private Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isBeforeDateTime(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            return true;
        }
        return date.equals(LocalDate.now()) && time.getStartAt().isBefore(LocalTime.now());
    }
}
