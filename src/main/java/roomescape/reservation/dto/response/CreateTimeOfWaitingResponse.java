package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public record CreateTimeOfWaitingResponse(Long id, LocalTime startAt) {
    public static CreateTimeOfWaitingResponse from(final ReservationTime reservationTime) {
        return new CreateTimeOfWaitingResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
