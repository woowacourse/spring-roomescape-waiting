package roomescape.controller.dto;


import java.util.List;

public record ReservationListResponse(
        List<ReservationResponse> reservations
) {

    public static ReservationListResponse from(List<ReservationResponse> reservations) {
        return new ReservationListResponse(reservations);
    }
}
