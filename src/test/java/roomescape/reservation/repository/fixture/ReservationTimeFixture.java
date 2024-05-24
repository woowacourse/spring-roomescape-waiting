package roomescape.reservation.repository.fixture;

import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public enum ReservationTimeFixture {

    TIME1(1L, "10:00"),
    TIME2(2L, "23:00"),
    TIME3(3L, "14:00"),
    TIME4(4L, "20:00"),
    ;

    private final long id;
    private final String startAt;


    ReservationTimeFixture(long id, String startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static int count() {
        return values().length;
    }

    public ReservationTime create() {
        return new ReservationTime(id, LocalTime.parse(startAt));
    }
}
