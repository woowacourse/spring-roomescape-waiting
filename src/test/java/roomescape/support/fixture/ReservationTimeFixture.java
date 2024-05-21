package roomescape.support.fixture;

import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public class ReservationTimeFixture {
    public static final ReservationTime TEN = startAt("10:00");

    public static ReservationTime startAt(String startAt) {
        return new ReservationTime(LocalTime.parse(startAt));
    }
}
