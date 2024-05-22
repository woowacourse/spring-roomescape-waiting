package roomescape.fixture;

import roomescape.domain.reservation.ReservationTime;
import roomescape.service.dto.input.ReservationTimeInput;

public class ReservationTimeFixture {
    public static ReservationTime getDomain() {
        return ReservationTime.from("11:00");
    }

    public static ReservationTimeInput reservationTimeInput() {
        return new ReservationTimeInput("10:00");
    }
}
