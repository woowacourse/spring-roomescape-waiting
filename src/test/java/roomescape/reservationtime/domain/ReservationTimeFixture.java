package roomescape.reservationtime.domain;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

public class ReservationTimeFixture {
    private static final AtomicLong identifier = new AtomicLong(1L);
    private static final LocalTime NOW = LocalTime.now();

    public static ReservationTime create() {
        long id = identifier.getAndIncrement();
        return new ReservationTime(
            id,
            NOW.plusNanos(id)
        );
    }

    public static ReservationTime createWithoutId() {
        long id = identifier.getAndIncrement();
        return new ReservationTime(
            NOW.plusNanos(id)
        );
    }
}
