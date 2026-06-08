package roomescape.dto.reservation.command;

import roomescape.domain.User;

public record DeleteReservationCommand(
        Long reservationId,
        User user
) {
    public static DeleteReservationCommand of(Long reservationId, User user) {
        return new DeleteReservationCommand(reservationId, user);
    }
}
