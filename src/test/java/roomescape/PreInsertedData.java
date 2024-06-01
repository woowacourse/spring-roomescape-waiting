package roomescape;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static roomescape.domain.Reservation.Status.RESERVED;
import static roomescape.domain.Reservation.Status.WAITING;

public class PreInsertedData {

    public static final ReservationTime TIME_10_O0 = new ReservationTime(
            1L,
            LocalTime.parse("10:00:00")
    );

    public static final ReservationTime TIME_11_00 = new ReservationTime(
            2L,
            LocalTime.parse("11:00:00")
    );

    public static final ReservationTime TIME_12_00 = new ReservationTime(
            3L,
            LocalTime.parse("12:00:00")
    );

    public static final Theme THEME_1 = new Theme(
            1L,
            "이름1",
            "설명1",
            "썸네일1"
    );

    public static final Theme THEME_2 = new Theme(
            2L,
            "이름2",
            "설명2",
            "썸네일2"
    );

    public static final Theme THEME_3 = new Theme(
            3L,
            "이름3",
            "설명3",
            "썸네일3"
    );

    public static final Member ADMIN = new Member(
            1L,
            "어드민",
            "admin@email.com",
            "admin",
            Role.ADMIN
    );

    public static final Member CUSTOMER_1 = new Member(
            2L,
            "고객1",
            "customer1@email.com",
            "customer1",
            Role.CUSTOMER
    );

    public static final Member CUSTOMER_2 = new Member(
            3L,
            "고객2",
            "customer2@email.com",
            "customer2",
            Role.CUSTOMER
    );

    public static final Member CUSTOMER_3 = new Member(
            4L,
            "고객3",
            "customer3@email.com",
            "customer3",
            Role.CUSTOMER
    );

    public static final Reservation RESERVATION_CUSTOMER1_THEME2_240501_1100 = new Reservation(
            1L,
            CUSTOMER_1,
            LocalDate.parse("2024-05-01"),
            TIME_11_00,
            THEME_2,
            RESERVED
    );

    public static final Reservation RESERVATION_CUSTOMER1_THEME3_240502_1100 = new Reservation(
            2L,
            CUSTOMER_1,
            LocalDate.parse("2024-05-02"),
            TIME_11_00,
            THEME_3,
            RESERVED
    );

    public static final Reservation RESERVATION_CUSTOMER1_THEME2_240501_1200 = new Reservation(
            3L,
            CUSTOMER_1,
            LocalDate.parse("2024-05-01"),
            TIME_12_00,
            THEME_2,
            RESERVED
    );

    public static final Reservation RESERVATION_CUSTOMER2_THEME3_240502_1200 = new Reservation(
            4L,
            CUSTOMER_2,
            LocalDate.parse("2024-05-02"),
            TIME_12_00,
            THEME_3,
            RESERVED
    );

    public static final Reservation RESERVATION_CUSTOMER2_THEME3_240503_1200 = new Reservation(
            5L,
            CUSTOMER_2,
            LocalDate.parse("2024-05-03"),
            TIME_12_00,
            THEME_3,
            RESERVED
    );

    public static final Reservation RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100 = new Reservation(
            6L,
            CUSTOMER_2,
            LocalDate.parse("2024-05-01"),
            TIME_11_00,
            THEME_2,
            WAITING
    );

    public static final Reservation RESERVATION_WAITING_CUSTOMER3_THEME2_240501_1100 = new Reservation(
            7L,
            CUSTOMER_3,
            LocalDate.parse("2024-05-01"),
            TIME_11_00,
            THEME_2,
            WAITING
    );

    public static final Reservation RESERVATION_WAITING_CUSTOMER1_THEME3_240502_1200 = new Reservation(
            8L,
            CUSTOMER_1,
            LocalDate.parse("2024-05-02"),
            TIME_12_00,
            THEME_3,
            WAITING
    );
}
