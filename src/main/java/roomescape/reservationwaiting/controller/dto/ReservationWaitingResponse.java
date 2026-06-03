package roomescape.reservationwaiting.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservationwaiting.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime requestAt,
        ReservationResponse reservationResponse
) {
    public static ReservationWaitingResponse from(final ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getRequestAt(),
                ReservationResponse.from(reservationWaiting.getReservation())
        );
    }
}
