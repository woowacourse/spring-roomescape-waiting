package roomescape.service.command;

import roomescape.domain.common.UserName;

import java.time.LocalDate;

public record ReservationCommand(
        UserName name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
