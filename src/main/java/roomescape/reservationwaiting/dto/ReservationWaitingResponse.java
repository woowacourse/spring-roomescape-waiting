package roomescape.reservationwaiting.dto;

import java.time.LocalDate;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingResponse(Long id, String name, LocalDate date) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(reservationWaiting.getId(), reservationWaiting.getName(),
                reservationWaiting.getDate());
    }
}
