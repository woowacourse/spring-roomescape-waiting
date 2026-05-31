package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;

public record ReservationSchedule(
        ReservationDate date,
        ReservationTime time
) {

    public boolean isPast(Clock clock) {
        return dateTime().isBefore(LocalDateTime.now(clock));
    }

    public boolean isToday(Clock clock) {
        return date.getPlayDay().isEqual(LocalDate.now(clock));
    }

    private LocalDateTime dateTime() {
        return LocalDateTime.of(date.getPlayDay(), time.getStartAt());
    }
}
