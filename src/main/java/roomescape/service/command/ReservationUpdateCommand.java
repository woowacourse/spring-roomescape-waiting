package roomescape.service.command;

import java.time.LocalDate;

public record ReservationUpdateCommand(
        LocalDate date,
        long timeId
) {
}
