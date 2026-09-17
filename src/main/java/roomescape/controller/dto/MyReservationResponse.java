package roomescape.controller.dto;

import java.util.List;

public record MyReservationResponse(
        List<ReservationResponse> reservationResponses,
        List<WaitingReservationResponse> waitingReservationResponses
) { }
