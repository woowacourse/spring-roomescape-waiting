package roomescape;

import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialReservationFixture.RESERVATION_1;

import roomescape.reservation.domain.Waiting;

public class InitialWaitingFixture {

    public static final int INITIAL_WAITING_COUNT = 1;
    public static Waiting WAITING_1 = new Waiting(
            1L,
            RESERVATION_1.getDate(),
            RESERVATION_1.getReservationTime(),
            RESERVATION_1.getTheme(),
            MEMBER_2
    );
}
