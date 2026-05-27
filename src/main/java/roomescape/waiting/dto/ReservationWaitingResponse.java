package roomescape.waiting.dto;

import roomescape.waiting.ReservationWaiting;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        Long themeId,
        LocalDate date,
        Long timeId,
        Long waitingNumber
) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getThemeId(),
                reservationWaiting.getDate(),
                reservationWaiting.getTimeId(),
                reservationWaiting.getWaitingNumber()
        );
    }
}
