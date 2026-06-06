package roomescape.controller.dto.response;

public record ReservationWaitListResponse(
        ReservationListResponse reservations,
        WaitListResponse waits
) {
}
