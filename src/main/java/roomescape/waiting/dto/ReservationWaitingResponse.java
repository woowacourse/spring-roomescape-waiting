package roomescape.waiting.dto;

import roomescape.time.ReservationTime;
import roomescape.waiting.ReservationWaiting;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        Long themeId,
        LocalDate date,
        ReservationTime reservationTime,
        Long waitingNumber
) {

    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getThemeId(),
                reservationWaiting.getDate(),
                reservationWaiting.getReservationTime(),
                reservationWaiting.getWaitingNumber()
        );
    }
}
