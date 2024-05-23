package roomescape.support.fixture;

import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public class ReservationTimeFixture {

    public static ReservationTime create() {
        return create("10:00");
    }

    public static ReservationTime create(String time) {
        return new ReservationTime(LocalTime.parse(time));
    }
}
