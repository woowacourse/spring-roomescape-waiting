package roomescape.domain;

import lombok.Getter;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.time.LocalDate;

@Getter
public class ReservationDate {

    LocalDate date;

    public ReservationDate(final LocalDate date) {
        validate(date);
        this.date = date;
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
