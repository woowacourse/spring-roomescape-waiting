package roomescape.reservationwaiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record AdminWaitingResponse(Long id, String memberName, LocalDate date, LocalTime startAt, String themeName) {

    public static AdminWaitingResponse from(ReservationWaiting waiting) {
        return new AdminWaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName()
        );
    }
}