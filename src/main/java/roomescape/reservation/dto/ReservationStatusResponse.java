package roomescape.reservation.dto;

public record ReservationStatusResponse(
        ReservationStatus type,
        Long rank
) {
}
