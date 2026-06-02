package roomescape.domain.fixture;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeStatus;

public class ReservationTimeFixture {

    public static ReservationTime createDefault() {
        return ReservationTime.restore(1L, LocalTime.of(10, 0), TimeStatus.ACTIVE);
    }

    public static ReservationTime createWithTime(LocalTime time) {
        return ReservationTime.restore(1L, time, TimeStatus.ACTIVE);
    }

    public static ReservationTime createInactive() {
        return ReservationTime.restore(1L, LocalTime.of(10, 0), TimeStatus.INACTIVE);
    }
}
