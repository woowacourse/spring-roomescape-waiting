package roomescape.admin.dto;

import java.time.LocalDate;
import roomescape.reservation.dto.request.ReservationRequest;

public record AdminReservationRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
    public ReservationRequest getReservationRequest() {
        return new ReservationRequest(date, timeId, themeId);
    }
}
