package roomescape.reservation.dto.command;

import java.time.LocalDate;

public record UpdateReservationCommand(
        LocalDate date,
        long timeId
) {
}
