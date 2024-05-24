package roomescape.support.fixture;

import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public class ReservationWaitingFixture {

    public static ReservationWaiting create(Reservation reservation, Member member) {
        return new ReservationWaiting(reservation, member);
    }
}
