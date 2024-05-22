package roomescape;

import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialMemberFixture.MEMBER_3;
import static roomescape.InitialReservationFixture.RESERVATION_1;

import roomescape.reservation.domain.Waiting;

public class InitialWaitingFixture {

    public static final int INITIAL_WAITING_COUNT = 2;
    public static final int MEMBER_2_INITIAL_WAITING_COUNT = 1;
    public static Waiting WAITING_1 = new Waiting(
            1L,
            RESERVATION_1.getDate(),
            RESERVATION_1.getReservationTime(),
            RESERVATION_1.getTheme(),
            MEMBER_2
    );

    public static Waiting WAITING_2 = new Waiting(
            2L,
            RESERVATION_1.getDate(),
            RESERVATION_1.getReservationTime(),
            RESERVATION_1.getTheme(),
            MEMBER_3
    );
}
