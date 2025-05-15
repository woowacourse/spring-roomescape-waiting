package roomescape.reservation.dto.response;

public record BookedReservationTimeResponse(
        ReservationTimeResponse timeResponse,
        boolean alreadyBooked
) {
}
