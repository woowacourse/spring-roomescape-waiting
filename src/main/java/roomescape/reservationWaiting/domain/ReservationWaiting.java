package roomescape.reservationWaiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservationWaiting.exception.ReservationWaitingErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

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

    public void validateExpiry() {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.of(this.date, this.time.startAt());

        if (current.isAfter(targetTime)) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.INVALID_TIME.getMessage());
        }
    }

    public void validateOwner(String userName) {
        if (!this.name.equals(userName)) {
            throw new ForbiddenException(ReservationWaitingErrorCode.AUTHORIZATION_FAIL.getMessage());
        }
    }
}
