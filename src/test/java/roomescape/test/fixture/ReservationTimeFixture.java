package roomescape.test.fixture;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.dto.business.ReservationTimeCreationContent;
import roomescape.dto.response.ReservationTimeResponse;

public class ReservationTimeFixture {

    public static ReservationTimeCreationContent createRequestDto(LocalTime time) {
        return new ReservationTimeCreationContent(time);
    }

    public static ReservationTime create(LocalTime time) {
        return ReservationTime.createWithoutId(time);
    }

    public static ReservationTimeResponse createResponseDto(ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime);
    }
}
