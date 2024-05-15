package roomescape.fixture;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

import java.time.LocalDate;

public class ReservationFixture {
    public static Reservation getNextDayReservation(ReservationTime time, Theme theme) {
        return new Reservation(null, LocalDate.now().plusDays(1), time, theme);
    }

    public static Reservation getNextMonthReservation(ReservationTime time, Theme theme) {
        return new Reservation(null, LocalDate.now().plusMonths(1), time, theme);
    }
}
