package roomescape.dto.command;

public record CancelReservationCommand(
        Long reservationId,
        Long userId
) {
}
