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

    public void validateNotExpired(LocalDateTime requestTime) {
        if (isDateBefore(requestTime.toLocalDate())) {
            throw new roomescape.global.exception.InvalidBusinessStateException(
                    roomescape.reservation.exception.ReservationErrorCode.INVALID_DATE);
        }
        if (isExpired(requestTime)) {
            throw new roomescape.global.exception.InvalidBusinessStateException(
                    roomescape.reservation.exception.ReservationErrorCode.INVALID_TIME);
        }
    }
}
