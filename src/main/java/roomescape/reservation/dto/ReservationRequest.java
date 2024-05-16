package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;

public record ReservationRequest(
        LocalDate date,
        long timeId,
        long themeId
) {
    public Reservation fromRequest(long memberId) {
        return Reservation.of(date, timeId, themeId, memberId);
    }
}
