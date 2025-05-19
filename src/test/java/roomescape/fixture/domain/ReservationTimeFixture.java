package roomescape.fixture.domain;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public class ReservationTimeFixture {

    public static ReservationTime NOT_SAVED_RESERVATION_TIME_1() {
        return new ReservationTime(LocalTime.of(10, 0));
    }

    public static ReservationTime NOT_SAVED_RESERVATION_TIME_2() {
        return new ReservationTime(LocalTime.of(11, 0));
    }

    public static ReservationTime NOT_SAVED_RESERVATION_TIME_3() {
        return new ReservationTime(LocalTime.of(12, 0));
    }
}
