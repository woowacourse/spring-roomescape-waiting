package roomescape.reservationWaiting.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.exception.InvalidReservationRequestFormatException;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;

public record ReservationWaitingRequest(String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationWaitingRequest {
        if (name == null || name.isBlank() ||
                date == null || timeId == null || themeId == null) {
            throw new InvalidReservationRequestFormatException();
        }
    }

    public ReservationWaitingCommand toCommand() {
        return new ReservationWaitingCommand(
                name,
                date,
                timeId,
                themeId
        );
    }
}
