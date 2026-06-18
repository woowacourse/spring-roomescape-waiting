package roomescape.controller.dto.response;

import java.time.LocalTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.domain.reservation.ReservationTime;

@Getter
@RequiredArgsConstructor
public class ReservationTimeResponse {
    private final long id;
    private final LocalTime startAt;

    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
