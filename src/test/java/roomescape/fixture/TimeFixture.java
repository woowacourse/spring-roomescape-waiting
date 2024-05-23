package roomescape.fixture;

import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public enum TimeFixture {
    TEN_AM(10, 0),
    ELEVEN_AM(11, 0),
    TWELVE_PM(12, 0),
    ONE_PM(13, 0),
    TWO_PM(14, 0),
    ;

    private final int hour;
    private final int minute;

    TimeFixture(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public ReservationTime create() {
        LocalTime time = LocalTime.of(hour, minute);
        return new ReservationTime(time);
    }
}
