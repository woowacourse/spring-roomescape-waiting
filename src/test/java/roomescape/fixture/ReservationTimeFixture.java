package roomescape.fixture;

import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public class ReservationTimeFixture {
    public static ReservationTime getNoon() {
        return new ReservationTime(1L, LocalTime.NOON);
    }

    public static ReservationTime get1PM() {
        return new ReservationTime(2L, LocalTime.of(13, 0));
    }

    public static ReservationTime get2PM() {
        return new ReservationTime(3L, LocalTime.of(14, 0));
    }
}
