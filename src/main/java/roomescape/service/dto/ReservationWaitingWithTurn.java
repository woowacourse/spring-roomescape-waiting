package roomescape.service.dto;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record ReservationWaitingWithTurn(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Long turn
) {
}
