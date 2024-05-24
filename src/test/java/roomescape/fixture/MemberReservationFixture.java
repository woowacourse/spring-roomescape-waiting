package roomescape.fixture;

import roomescape.reservation.domain.Reservation;

public class MemberReservationFixture {
    public static Reservation getBookedMemberReservation() {
        return new Reservation(1L, MemberFixture.getMemberChoco(), ReservationFixture.getNextMonthReservation(ReservationTimeFixture.get2PM(), ThemeFixture.getTheme2()));
    }
}
