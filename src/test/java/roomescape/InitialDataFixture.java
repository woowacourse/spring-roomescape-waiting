package roomescape;

import static roomescape.InitialMemberFixture.LOGIN_MEMBER_1;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_2;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_3;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public class InitialDataFixture {

    public static final int INITIAL_THEME_COUNT = 3;
    public static final int INITIAL_RESERVATION_TIME_COUNT = 3;
    public static final int INITIAL_RESERVATION_COUNT = 6;
    public static final int THEME2_MEMBER1_RESERVATION_COUNT = 3;
    public static final ReservationTime RESERVATION_TIME_1 = new ReservationTime(1L, LocalTime.parse("09:00"));
    public static final ReservationTime RESERVATION_TIME_2 = new ReservationTime(2L, LocalTime.parse("10:00"));
    public static final ReservationTime NOT_RESERVATION_TIME = new ReservationTime(3L, LocalTime.parse("11:00"));
    public static final LocalDate NO_RESERVATION_DATE = LocalDate.parse("2020-01-01");
    public static final LocalDate TRENDING_STATS_START = LocalDate.parse("2024-04-25");
    public static final LocalDate TRENDING_STATS_END = LocalDate.parse("2020-04-26");

    public static final Theme THEME_1 = new Theme(
            1L,
            new Name("레벨1 탈출"),
            "우테코 레벨1를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme THEME_2 = new Theme(
            2L,
            new Name("레벨2 탈출"),
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme NOT_RESERVATION_THEME = new Theme(
            3L,
            new Name("레벨3 탈출"),
            "우테코 레벨3를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );

    public static final Reservation RESERVATION_1 = new Reservation(
            1L,
            LocalDate.parse("2024-04-25"),
            RESERVATION_TIME_1,
            THEME_1,
            LOGIN_MEMBER_2
    );
    public static final Reservation RESERVATION_2 = new Reservation(
            2L,
            LocalDate.parse("2099-05-01"),
            RESERVATION_TIME_1,
            THEME_1,
            LOGIN_MEMBER_3
    );
    public static final Reservation RESERVATION_3 = new Reservation(
            3L,
            LocalDate.parse("2024-04-25"),
            RESERVATION_TIME_2,
            THEME_1,
            LOGIN_MEMBER_1
    );
    public static final Reservation RESERVATION_4 = new Reservation(
            4L,
            LocalDate.parse("2024-04-25"),
            RESERVATION_TIME_2,
            THEME_2,
            LOGIN_MEMBER_1
    );
    public static final Reservation RESERVATION_5 = new Reservation(
            5L,
            LocalDate.parse("2024-04-27"),
            RESERVATION_TIME_1,
            THEME_2,
            LOGIN_MEMBER_1
    );
    public static final Reservation RESERVATION_6 = new Reservation(
            6L,
            LocalDate.parse("2024-04-27"),
            RESERVATION_TIME_2,
            THEME_2,
            LOGIN_MEMBER_1
    );
}
