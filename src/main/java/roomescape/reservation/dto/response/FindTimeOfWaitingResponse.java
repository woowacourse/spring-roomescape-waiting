package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public record FindTimeOfWaitingResponse(Long id, LocalTime startAt) {
    public static FindTimeOfWaitingResponse from(ReservationTime reservationTime) {
        return new FindTimeOfWaitingResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
