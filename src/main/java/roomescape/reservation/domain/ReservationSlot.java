package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record ReservationSlot(
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
    public boolean isDateBefore(LocalDate otherDate) {
        return this.date.isBefore(otherDate);
    }

    public boolean isExpired(LocalDateTime requestTime) {
        LocalDateTime startDateTime = LocalDateTime.of(this.date, this.time.getStartAt());
        return requestTime.isAfter(startDateTime);
    }
}
