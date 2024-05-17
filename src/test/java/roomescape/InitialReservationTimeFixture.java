package roomescape;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public class InitialReservationTimeFixture {

    public static final int INITIAL_RESERVATION_TIME_COUNT = 4;
    public static final ReservationTime RESERVATION_TIME_1 = new ReservationTime(1L, LocalTime.parse("09:00"));
    public static final ReservationTime RESERVATION_TIME_2 = new ReservationTime(2L, LocalTime.parse("10:00"));
    public static final ReservationTime RESERVATION_TIME_3 = new ReservationTime(3L, LocalTime.parse("11:00"));
    public static final ReservationTime NOT_RESERVATED_TIME = new ReservationTime(4L, LocalTime.parse("12:00"));
    public static final ReservationTime NOT_SAVED_TIME = new ReservationTime(null, LocalTime.parse("13:00"));
}
