package roomescape.domain;

import lombok.Getter;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReservationDate {

    private static final int RESERVABLE_DAYS_RANGE = 14;

    private final LocalDate date;

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

    public static List<LocalDate> getReservableDates() {
        final LocalDate today = LocalDate.now();
        return today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).collect(Collectors.toList());
    }
}
