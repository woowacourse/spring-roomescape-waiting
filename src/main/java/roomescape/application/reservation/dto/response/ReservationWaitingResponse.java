package roomescape.application.reservation.dto.response;

public record ReservationWaitingResponse(
        ReservationResponse reservation,
        long waitingCount
) {
}
