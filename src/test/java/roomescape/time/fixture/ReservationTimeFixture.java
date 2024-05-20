package roomescape.time.fixture;

import static roomescape.time.fixture.DateTimeFixture.TIME_10_00;
import static roomescape.time.fixture.DateTimeFixture.TIME_11_00;
import static roomescape.time.fixture.DateTimeFixture.TIME_12_00;

import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.ReservationTimeAddRequest;

public class ReservationTimeFixture {
    public static final ReservationTimeAddRequest TIME_ADD_REQUEST_10_00 = new ReservationTimeAddRequest(TIME_10_00);

    public static final ReservationTime RESERVATION_TIME_10_00_ID_1 = new ReservationTime(1L, TIME_10_00);
    public static final ReservationTime RESERVATION_TIME_11_00_ID_2 = new ReservationTime(2L, TIME_11_00);
    public static final ReservationTime RESERVATION_TIME_3 = new ReservationTime(3L, TIME_12_00);
}
