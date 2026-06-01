package roomescape.feature.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.feature.time.domain.Time;

public record Schedule(
        LocalDate date,
        Time time
) {

    public boolean isPast() {
        LocalDateTime schedule = LocalDateTime.of(date, time.getStartAt());

        return schedule.isBefore(LocalDateTime.now());
    }
}
