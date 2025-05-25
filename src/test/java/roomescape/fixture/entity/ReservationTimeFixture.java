package roomescape.fixture.entity;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public class ReservationTimeFixture {

    public static final LocalTime START_AT = LocalTime.of(10, 0);

    public static ReservationTime create() {
        return ReservationTime.open(START_AT);
    }

    public static ReservationTime create(LocalTime startAt) {
        return ReservationTime.open(startAt);
    }
}
