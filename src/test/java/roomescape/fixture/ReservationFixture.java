package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public class ReservationFixture {
    public static ReservationSlot getNextDayReservation(ReservationTime time, Theme theme) {
        return new ReservationSlot(null, LocalDate.now().plusDays(1), time, theme);
    }

    public static ReservationSlot getNextMonthReservation(ReservationTime time, Theme theme) {
        return new ReservationSlot(null, LocalDate.now().plusMonths(1), time, theme);
    }
}
