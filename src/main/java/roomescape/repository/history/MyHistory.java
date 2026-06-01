package roomescape.repository.history;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record MyHistory(
        Long reservationId,
        Long waitingId,
        String status,
        String name,
        LocalDate date,
        Theme theme,
        ReservationTime time,
        LocalDateTime requestedAt
) {
}
