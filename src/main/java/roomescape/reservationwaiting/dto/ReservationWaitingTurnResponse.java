package roomescape.reservationwaiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record ReservationWaitingTurnResponse(Long id, String memberName, LocalDate date, LocalTime startAt,
                                             String themeName, Long turn) {

    public static ReservationWaitingTurnResponse from(ReservationWaiting waiting, Long turn) {
        return new ReservationWaitingTurnResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName(),
                turn
        );
    }
}