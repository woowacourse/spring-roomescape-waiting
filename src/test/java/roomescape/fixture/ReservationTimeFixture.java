package roomescape.fixture;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;

public class ReservationTimeFixture {
    public static final ReservationTime DEFAULT_TIME = new ReservationTime(1L, LocalTime.of(11, 56));
    public static final ReservationTimeRequest DEFAULT_REQUEST = new ReservationTimeRequest(DEFAULT_TIME.getStartAt());
    public static final ReservationTimeResponse DEFAULT_RESPONSE = new ReservationTimeResponse(DEFAULT_TIME.getId(),
            DEFAULT_TIME.getStartAt());
}
