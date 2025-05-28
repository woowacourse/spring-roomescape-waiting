package roomescape.admin.presentation.dto;

import java.time.LocalDate;
import roomescape.reservation.presentation.dto.request.ReservationRequest;

public record AdminReservationRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
    public ReservationRequest getReservationRequest() {
        return new ReservationRequest(date, timeId, themeId);
    }
}
