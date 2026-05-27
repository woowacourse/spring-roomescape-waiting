package roomescape.dto.response;

import java.util.List;

public record MyReservationsAndWaitsResponse(
        List<ReservationResponse> reservations,
        List<WaitingResponse> waitings
) {
}
