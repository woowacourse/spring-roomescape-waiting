package roomescape.reservationwaiting.dto;

import java.time.LocalDate;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingResponse(Long id, String memberName, LocalDate date, Long timeId, Long themeId) {

    public static ReservationWaitingResponse from(ReservationWaiting waiting) {
        return new ReservationWaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        );
    }
}