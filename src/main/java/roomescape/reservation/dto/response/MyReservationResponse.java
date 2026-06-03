package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationStatus;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeSimpleResponse theme,
        ReservationStatus status,
        Long waitRank
) {
}
