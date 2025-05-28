package roomescape.business.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.business.InvalidCreateArgumentException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static roomescape.exception.ErrorCode.RESERVATION_DATE_PAST;
import static roomescape.exception.ErrorCode.RESERVATION_DATE_TOO_FAR_IN_FUTURE;

@Embeddable
public record ReservationDate(
        @Column(name = "reservation_date")
        LocalDate value
) {
    private static final int INTERVAL_FROM_NOW = 7;

    public void validateFresh() {
        validateNotPast(value);
        validateInterval(value);
    }

    private static void validateNotPast(final LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidCreateArgumentException(RESERVATION_DATE_PAST);
        }
    }

    private static void validateInterval(final LocalDate date) {
        long minusDays = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (minusDays > INTERVAL_FROM_NOW) {
            throw new InvalidCreateArgumentException(RESERVATION_DATE_TOO_FAR_IN_FUTURE, INTERVAL_FROM_NOW);
        }
    }
}
