package roomescape.service.result;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record WaitingResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Long turn
) {
}
