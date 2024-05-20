package roomescape.fixture;

import static roomescape.fixture.LocalTimeFixture.TEN_HOUR;

import roomescape.domain.reservation.domain.reservationTime.ReservationTime;

public class ReservationTimeFixture {

    public static final ReservationTime NULL_ID_RESERVATION_TIME = new ReservationTime(null, TEN_HOUR);
    public static final ReservationTime TEN_RESERVATION_TIME = new ReservationTime(1L, TEN_HOUR);
}
