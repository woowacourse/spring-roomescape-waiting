package roomescape.fixture;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public class ReservationTimeFixture {

    public static ReservationTime getOne() {
        return new ReservationTime(LocalTime.parse("10:00"));
    }
}
