package roomescape.fixture;


import java.time.LocalTime;

import roomescape.entity.ReservationTime;

public class ReservationTimeFixture {
    public static final ReservationTime DEFAULT_RESERVATION_TIME = new ReservationTime(1L, LocalTime.of(11, 30));
}
