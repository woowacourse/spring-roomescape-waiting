package roomescape.fixture;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.TimeCreateRequest;

public class TimeFixture {
    public static final ReservationTime TIME_1 = new ReservationTime(LocalTime.of(10, 0));
    public static final ReservationTime TIME_2 = new ReservationTime(LocalTime.of(19, 0));
    public static final ReservationTime TIME_3 = new ReservationTime(LocalTime.of(21, 0));

    public static TimeCreateRequest toTimeCreateRequest(ReservationTime time) {
        return new TimeCreateRequest(time.getStartAt());
    }
}
