package roomescape.fixture;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservationtime.model.ReservationTime;

public class ReservationTimeFixture {

    public static ReservationTime getOne() {
        return new ReservationTime(null, LocalTime.parse("10:00"));
    }

    public static List<ReservationTime> get(int count) {
        final List<ReservationTime> reservationTimes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            reservationTimes.add(new ReservationTime(
                    null,
                    LocalTime.of(10, count))
            );
        }

        return reservationTimes;
    }
}
