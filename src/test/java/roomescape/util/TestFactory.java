package roomescape.util;

import org.springframework.test.util.ReflectionTestUtils;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.ReservationTime;

public class TestFactory {

    public static Reservation reservationWithId(Long id, Reservation reservation) {
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    public static ReservationTime reservationTimeWithId(Long id, ReservationTime reservationTime) {
        ReflectionTestUtils.setField(reservationTime, "id", id);
        return reservationTime;
    }
}
