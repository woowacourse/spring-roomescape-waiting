package roomescape;

import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialReservationTimeFixture.RESERVATION_TIME_1;
import static roomescape.InitialReservationTimeFixture.RESERVATION_TIME_2;
import static roomescape.InitialReservationTimeFixture.RESERVATION_TIME_3;
import static roomescape.InitialThemeFixture.THEME_1;
import static roomescape.InitialThemeFixture.THEME_2;
import static roomescape.InitialThemeFixture.THEME_3;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;

public class InitialReservationFixture {

    public static final int INITIAL_RESERVATION_COUNT = 5;
    public static final int THEME3_MEMBER1_RESERVATION_COUNT = 3;
    public static final LocalDate NO_RESERVATION_DATE = LocalDate.parse("2020-01-01");
    public static final Reservation RESERVATION_1 = new Reservation(
            1L,
            LocalDate.now().minusDays(10),
            RESERVATION_TIME_1,
            THEME_3,
            MEMBER_1
    );
    public static final Reservation RESERVATION_2 = new Reservation(
            2L,
            LocalDate.now().minusDays(6),
            RESERVATION_TIME_2,
            THEME_3,
            MEMBER_1
    );
    public static final Reservation RESERVATION_3 = new Reservation(
            3L,
            LocalDate.now().minusDays(5),
            RESERVATION_TIME_3,
            THEME_3,
            MEMBER_1
    );
    public static final Reservation RESERVATION_4 = new Reservation(
            4L,
            LocalDate.now().minusDays(2),
            RESERVATION_TIME_2,
            THEME_2,
            MEMBER_2
    );
    public static final Reservation RESERVATION_5 = new Reservation(
            5L,
            LocalDate.now().minusDays(1),
            RESERVATION_TIME_1,
            THEME_1,
            MEMBER_2
    );
}
