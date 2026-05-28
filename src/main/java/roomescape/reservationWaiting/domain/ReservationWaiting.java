package roomescape.reservationWaiting.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidRequestValueException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;

public record ReservationWaiting(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
    public static ReservationWaiting of(String name, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationWaiting(null, name, date, time, theme);
    }

    public ReservationWaiting withId(Long id) {
        return new ReservationWaiting(id, this.name, this.date, this.time, this.theme);
    }

    public void validateExpiry(Clock clock) {
        LocalDate nowDate = LocalDate.now(clock);
        if (nowDate.isAfter(date)) {
            throw new InvalidRequestValueException(ReservationErrorCode.INVALID_DATE.getMessage());
        }
        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(time.startAt())) {
            throw new InvalidRequestValueException(TimeErrorCode.INVALID_START_AT.getMessage());
        }
    }

    public void validateOwner(String userName) {
        if (!this.name.equals(userName)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
        }
    }
}
