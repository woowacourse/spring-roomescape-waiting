package roomescape.fixture;

import roomescape.reservation.domain.MemberReservation;

public class MemberReservationFixture {
    public static MemberReservation getBookedMemberReservation() {
        return new MemberReservation(1L, MemberFixture.getMemberChoco(), ReservationFixture.getNextMonthReservation(ReservationTimeFixture.get2PM(), ThemeFixture.getTheme2()));
    }
}
