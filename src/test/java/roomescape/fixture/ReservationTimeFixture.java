package roomescape.fixture;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public class ReservationTimeFixture {

    private ReservationTimeFixture() {
    }

    public static ReservationTime create() {
        return new ReservationTime(1L, LocalTime.of(10, 0));
    }
}
