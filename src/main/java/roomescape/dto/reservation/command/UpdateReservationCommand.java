package roomescape.dto.reservation.command;

import java.time.LocalDate;
import roomescape.dto.reservation.request.UpdateReservationRequest;

public record UpdateReservationCommand(
        Long reservationId,
        Long userId,
        Long themeId,
        LocalDate date,
        Long timeId
) {
    public static UpdateReservationCommand of(Long reservationId, Long userId, UpdateReservationRequest request) {
        return new UpdateReservationCommand(
                reservationId, userId, request.themeId(), request.date(), request.timeId());
    }
}
