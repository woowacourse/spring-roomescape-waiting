package roomescape.domain.reservation.dto.command;

import java.time.LocalDate;

public record ReservationUpdateCommand(
    LocalDate date,
    Long timeId,
    Long themeId,
    Long version
) {

}
