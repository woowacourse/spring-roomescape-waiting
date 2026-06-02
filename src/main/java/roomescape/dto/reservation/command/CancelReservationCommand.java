package roomescape.dto.reservation.command;

import roomescape.domain.User;

public record CancelReservationCommand(
        Long reservationId,
        User user
) {
    public static CancelReservationCommand of(Long reservationId, User user) {
        return new CancelReservationCommand(reservationId, user);
    }
}
