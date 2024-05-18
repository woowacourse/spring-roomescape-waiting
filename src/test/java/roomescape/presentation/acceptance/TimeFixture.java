package roomescape.presentation.acceptance;

import java.time.LocalTime;
import roomescape.domain.Time;

class TimeFixture {

    static Time defaultValue() {
        return of(0, 0);
    }

    static Time of(int time, int minute) {
        return new Time(LocalTime.of(time, minute));
    }
}
