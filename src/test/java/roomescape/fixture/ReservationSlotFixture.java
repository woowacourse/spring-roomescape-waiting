package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public class ReservationSlotFixture {
    public static ReservationSlot getReservationSlot1() {
        return new ReservationSlot(1L, LocalDate.now().plusMonths(1), ReservationTimeFixture.get2PM(), ThemeFixture.getTheme2());
    }

    public static ReservationSlot getNextDayReservationSlot(ReservationTime time, Theme theme) {
        return new ReservationSlot(null, LocalDate.now().plusDays(1), time, theme);
    }

    public static ReservationSlot getNextMonthReservationSlot(ReservationTime time, Theme theme) {
        return new ReservationSlot(null, LocalDate.now().plusMonths(1), time, theme);
    }

    public static ReservationSlot getTestYearReservationSlot(ReservationTime time, Theme theme) {
        return new ReservationSlot(null, LocalDate.now().plusYears(999), time, theme);
    }
}
