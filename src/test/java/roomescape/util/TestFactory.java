package roomescape.util;

import org.springframework.test.util.ReflectionTestUtils;
import roomescape.reservation.Reservation;

public class TestFactory {

    public static Reservation reservationWithId(Long id, Reservation reservation) {
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }
}
