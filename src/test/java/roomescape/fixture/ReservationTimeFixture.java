package roomescape.fixture;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public class ReservationTimeFixture {

    public static final Long ID = 1L;
    public static final LocalTime START_AT = LocalTime.of(12, 0);
    public static final ReservationTime SAVED = ReservationTime.of(ID, START_AT);

    public static ReservationTime savedWith(Long id, LocalTime startAt) {
        return ReservationTime.of(id, startAt);
    }
}
