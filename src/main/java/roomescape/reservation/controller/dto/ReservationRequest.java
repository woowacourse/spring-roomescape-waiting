package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.exception.InvalidReservationNameException;
import roomescape.reservation.exception.InvalidReservationRequestFormatException;
import roomescape.reservation.service.dto.ReservationCommand;

public record ReservationRequest(String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        if (date == null || timeId == null || themeId == null) {
            throw new InvalidReservationRequestFormatException();
        }

        if (name == null || name.isBlank() ||
                !name.matches("^[a-zA-Z]+$")) {
            throw new InvalidReservationNameException();
        }
    }

    public ReservationCommand toCommand() {
        return new ReservationCommand(
                name,
                date,
                timeId,
                themeId
        );
    }
}
