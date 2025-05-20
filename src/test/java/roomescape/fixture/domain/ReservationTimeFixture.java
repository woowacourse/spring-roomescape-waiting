package roomescape.fixture.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.ReservationTime;

public class ReservationTimeFixture {

    public static List<ReservationTime> notSavedReservationTimes(int count) {
        List<ReservationTime> reservationTimes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reservationTimes.add(new ReservationTime(LocalTime.of(10 + i / 60, i % 60)));
        }
        return reservationTimes;
    }

    public static ReservationTime notSavedReservationTime1() {
        return new ReservationTime(LocalTime.of(10, 0));
    }

    public static ReservationTime notSavedReservationTime2() {
        return new ReservationTime(LocalTime.of(11, 0));
    }

    public static ReservationTime notSavedReservationTime3() {
        return new ReservationTime(LocalTime.of(12, 0));
    }
}
