package roomescape.fixture;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public class TimeFixture {

    public static final ReservationTime TIME = new ReservationTime(
            1L,
            LocalTime.of(10, 0)
    );
}
