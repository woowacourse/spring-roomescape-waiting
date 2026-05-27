package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record ReservationWithStatusResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        String status,
        Long waitingOrder
) {

}
