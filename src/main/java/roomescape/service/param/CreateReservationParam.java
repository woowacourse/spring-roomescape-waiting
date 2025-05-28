package roomescape.service.param;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;

public record CreateReservationParam(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId,
        ReservationStatus status
) {
}
