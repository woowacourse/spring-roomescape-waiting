package roomescape.dto.reservation;

public record CancelReservationCommand(
        Long reservationId,
        Long userId
) {
}
