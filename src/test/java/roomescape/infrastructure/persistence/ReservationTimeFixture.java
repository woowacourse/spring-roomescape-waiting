package roomescape.infrastructure.persistence;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

class ReservationTimeFixture {

    static ReservationTime defaultValue() {
        return of(0, 0);
    }

    static ReservationTime of(int time, int minute) {
        return new ReservationTime(LocalTime.of(time, minute));
    }
}
