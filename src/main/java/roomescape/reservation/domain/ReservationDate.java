package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.reservation.exception.ReservationErrorCode;

public record ReservationDate(LocalDate date) {

    public static ReservationDate of(LocalDate date, Clock clock) {
        if (LocalDate.now(clock).isAfter(date)) {
            throw new InvalidRequestValueException(ReservationErrorCode.INVALID_DATE.getMessage());
        }
        return new ReservationDate(date);
    }
}
