package roomescape.fixture.domain;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public class ReservationTimeFixture {

    public static final ReservationTime NOT_SAVED_RESERVATION_TIME_1 = new ReservationTime(LocalTime.of(10, 0));
    public static final ReservationTime NOT_SAVED_RESERVATION_TIME_2 = new ReservationTime(LocalTime.of(11, 0));
    public static final ReservationTime NOT_SAVED_RESERVATION_TIME_3 = new ReservationTime(LocalTime.of(12, 0));
    public static final ReservationTime NOT_SAVED_RESERVATION_TIME_4 = new ReservationTime(LocalTime.of(13, 0));
}
