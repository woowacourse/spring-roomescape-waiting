package roomescape.fixture;

import roomescape.reservation.domain.Reservation;

public class ReservationFixture {
    public static Reservation getBookedReservation() {
        return new Reservation(1L, MemberFixture.getMemberChoco(), ReservationSlotFixture.getNextMonthReservationSlot(ReservationTimeFixture.get2PM(), ThemeFixture.getTheme2()));
    }
}
