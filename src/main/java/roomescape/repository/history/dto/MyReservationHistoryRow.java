package roomescape.repository.history.dto;

import java.time.LocalDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record MyReservationHistoryRow(
        Long reservationId,
        Long waitingId,
        String status,
        String name,
        LocalDate date,
        Theme theme,
        ReservationTime time,
        Integer sequence
) {
}
