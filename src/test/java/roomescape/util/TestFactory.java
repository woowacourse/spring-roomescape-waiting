package roomescape.util;

import org.springframework.test.util.ReflectionTestUtils;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public class TestFactory {

    public static Reservation reservationWithId(Long id, Reservation reservation) {
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    public static ReservationTime reservationTimeWithId(Long id, ReservationTime reservationTime) {
        ReflectionTestUtils.setField(reservationTime, "id", id);
        return reservationTime;
    }

    public static Theme themeWithId(Long id, Theme theme) {
        ReflectionTestUtils.setField(theme, "id", id);
        return theme;
    }
}
