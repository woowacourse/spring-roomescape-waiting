package roomescape.reservation.controller.dto;



import java.util.List;

public record ReservationWaitingListResponse(
        List<ReservationWaitingResponse> reservations
) {

    public static ReservationWaitingListResponse from(List<ReservationWaitingResponse> reservations) {
        return new ReservationWaitingListResponse(reservations);
    }
}
