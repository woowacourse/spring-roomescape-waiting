package roomescape.domain.fixture;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeStatus;
import roomescape.support.TestDateTimes;

public class ReservationTimeFixture {

    public static ReservationTime createDefault() {
        return ReservationTime.restore(1L, TestDateTimes.defaultTime(), TimeStatus.ACTIVE);
    }

    public static ReservationTime createWithTime(LocalTime time) {
        return ReservationTime.restore(1L, time, TimeStatus.ACTIVE);
    }

    public static ReservationTime createInactive() {
        return ReservationTime.restore(1L, TestDateTimes.defaultTime(), TimeStatus.INACTIVE);
    }
}
