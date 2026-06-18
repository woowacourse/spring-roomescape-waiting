package roomescape.domain.reservation.dto.command;

import java.time.LocalDate;

public record ReservationCreateCommand(
    String name,
    LocalDate date,
    Long timeId,
    Long themeId
) {

}
