package roomescape.dto.reservation.command;

public record CancelReservationCommand(
        Long reservationId,
        Long userId
) {
    public static CancelReservationCommand of(Long reservationId, Long userId) {
        return new CancelReservationCommand(reservationId, userId);
    }
}
