package roomescape.domain;

import java.time.LocalDate;
import java.util.List;

public class ReservationPolicy {

    private ReservationPolicy() {}

    private static final int RESERVABLE_DAYS_RANGE = 14;

    public static List<LocalDate> getReservableDates() {
        final LocalDate today = LocalDate.now();
        return today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).toList();
    }
}
