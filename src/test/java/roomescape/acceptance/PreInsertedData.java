package roomescape.acceptance;

import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class PreInsertedData {

    public static final ReservationTime PRE_INSERTED_RESERVATION_TIME_1 = new ReservationTime(
            1L,
            LocalTime.parse("10:00:00")
    );

    public static final ReservationTime PRE_INSERTED_RESERVATION_TIME_2 = new ReservationTime(
            2L,
            LocalTime.parse("11:00:00")
    );

    public static final ReservationTime PRE_INSERTED_RESERVATION_TIME_3 = new ReservationTime(
            3L,
            LocalTime.parse("12:00:00")
    );

    public static final Theme PRE_INSERTED_THEME_1 = new Theme(
            1L,
            "이름1",
            "설명1",
            "썸네일1"
    );

    public static final Theme PRE_INSERTED_THEME_2 = new Theme(
            2L,
            "이름2",
            "설명2",
            "썸네일2"
    );

    public static final Theme PRE_INSERTED_THEME_3 = new Theme(
            3L,
            "이름3",
            "설명3",
            "썸네일3"
    );

    public static final Member PRE_INSERTED_ADMIN = new Member(
            1L,
            "어드민",
            "admin@email.com",
            "admin",
            Role.ADMIN
    );

    public static final Member PRE_INSERTED_CUSTOMER_1 = new Member(
            2L,
            "고객1",
            "customer1@email.com",
            "customer1",
            Role.CUSTOMER
    );

    public static final Member PRE_INSERTED_CUSTOMER_2 = new Member(
            3L,
            "고객2",
            "customer2@email.com",
            "customer2",
            Role.CUSTOMER
    );


    public static final Reservation PRE_INSERTED_RESERVATION_1 = new Reservation(
            1L,
            PRE_INSERTED_CUSTOMER_1,
            LocalDate.parse("2024-05-01"),
            PRE_INSERTED_RESERVATION_TIME_2,
            PRE_INSERTED_THEME_2
    );

    public static final Reservation PRE_INSERTED_RESERVATION_2 = new Reservation(
            2L,
            PRE_INSERTED_CUSTOMER_1,
            LocalDate.parse("2024-05-02"),
            PRE_INSERTED_RESERVATION_TIME_2,
            PRE_INSERTED_THEME_3
    );

    public static final Reservation PRE_INSERTED_RESERVATION_3 = new Reservation(
            3L,
            PRE_INSERTED_CUSTOMER_1,
            LocalDate.parse("2024-05-01"),
            PRE_INSERTED_RESERVATION_TIME_3,
            PRE_INSERTED_THEME_2
    );

    public static final Reservation PRE_INSERTED_RESERVATION_4 = new Reservation(
            4L,
            PRE_INSERTED_CUSTOMER_2,
            LocalDate.parse("2024-05-02"),
            PRE_INSERTED_RESERVATION_TIME_3,
            PRE_INSERTED_THEME_3
    );
}
