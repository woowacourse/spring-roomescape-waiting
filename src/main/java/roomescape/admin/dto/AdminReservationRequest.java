package roomescape.admin.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;

public record AdminReservationRequest(LocalDate date, long themeId, long timeId, long memberId) {

    public Reservation fromRequest() {
        return Reservation.saveReservationOf(date, timeId, themeId, memberId);
    }
}
