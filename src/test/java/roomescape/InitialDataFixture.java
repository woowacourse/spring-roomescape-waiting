package roomescape;

import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public class InitialDataFixture {

    public static final int INITIAL_THEME_COUNT = 4;
    public static final int INITIAL_RESERVATION_TIME_COUNT = 4;
    public static final int INITIAL_RESERVATION_COUNT = 5;
    public static final int THEME3_MEMBER1_RESERVATION_COUNT = 3;
    public static final LocalDate NO_RESERVATION_DATE = LocalDate.parse("2020-01-01");

    public static final ReservationTime RESERVATION_TIME_1 = new ReservationTime(1L, LocalTime.parse("09:00"));
    public static final ReservationTime RESERVATION_TIME_2 = new ReservationTime(2L, LocalTime.parse("10:00"));
    public static final ReservationTime RESERVATION_TIME_3 = new ReservationTime(3L, LocalTime.parse("11:00"));
    public static final ReservationTime NOT_RESERVATED_TIME = new ReservationTime(4L, LocalTime.parse("12:00"));

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
    public static final Theme THEME_3 = new Theme(
            3L,
            new Name("레벨3 탈출"),
            "우테코 레벨3를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme NOT_RESERVED_THEME = new Theme(
            4L,
            new Name("레벨4 탈출"),
            "우테코 레벨4를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme NOT_SAVED_THEME = new Theme(
            null,
            new Name("not saved theme name"),
            "any description",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );

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
