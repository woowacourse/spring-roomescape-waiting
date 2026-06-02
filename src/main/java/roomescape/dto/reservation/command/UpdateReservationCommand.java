package roomescape.dto.reservation.command;

import java.time.LocalDate;
import roomescape.domain.User;
import roomescape.dto.reservation.request.UpdateReservationRequest;

public record UpdateReservationCommand(
        Long reservationId,
        User user,
        Long themeId,
        LocalDate date,
        Long timeId
) {
    public static UpdateReservationCommand of(Long reservationId, User user, UpdateReservationRequest request) {
        return new UpdateReservationCommand(
                reservationId, user, request.themeId(), request.date(), request.timeId());
    }
}
