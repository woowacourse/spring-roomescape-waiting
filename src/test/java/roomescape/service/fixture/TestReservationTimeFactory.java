package roomescape.service.fixture;

import java.time.LocalTime;

import roomescape.model.ReservationTime;

public class TestReservationTimeFactory {

    public static ReservationTime createReservationTime(Long id, String time) {
        return new ReservationTime(id, LocalTime.parse(time));
    }

    public static ReservationTime createReservationTime(String time) {
        return new ReservationTime(LocalTime.parse(time));
    }

    public static ReservationTime createReservationTime(Long id, LocalTime time) {
        return new ReservationTime(id, time);
    }
}
