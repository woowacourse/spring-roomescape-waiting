package roomescape.reservation.dto;


import java.util.List;

public record ReservationsResponse(
        List<ReservationResponse> reservations
) {

    public static ReservationsResponse from(List<ReservationResponse> reservations) {
        return new ReservationsResponse(reservations);
    }
}
