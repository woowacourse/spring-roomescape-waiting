package roomescape.reservation.dto;

public record BookedReservationTimeResponse(
        ReservationTimeResponse timeResponse,
        boolean alreadyBooked
) {
}
