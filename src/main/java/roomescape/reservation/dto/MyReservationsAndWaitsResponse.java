package roomescape.reservation.dto;

import java.util.List;
import roomescape.reservationwait.dto.WaitingResponse;

public record MyReservationsAndWaitsResponse(
        List<ReservationResponse> reservations,
        List<WaitingResponse> waitings
) {
}
