package roomescape.domain;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.time.LocalDate;

public record ReservationDate(
        LocalDate date
) {

    public ReservationDate {
        validate(date);
    }

    private void validate(final LocalDate date) {
        if (date == null) {
            throw new BusinessException(ErrorCode.DATE_NULL);
        }
    }

    public boolean isPast() {
        return date.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return date.isEqual(LocalDate.now());
    }
}
