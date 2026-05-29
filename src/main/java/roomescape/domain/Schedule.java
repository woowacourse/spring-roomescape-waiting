package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Schedule(
        LocalDate date,
        ReservationTime time
) {
    public static Schedule from(LocalDate date, ReservationTime time) {
        return new Schedule(date, time);
    }

    public boolean isPast(LocalDateTime now) {
        return date.atTime(time.startAt()).isBefore(now);
    }
}
