package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationAndWaiting;

public record ReservationAndWaitingResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        boolean isReserved,
        Integer waitingNumber
) {

    public static ReservationAndWaitingResponse from(ReservationAndWaiting reservationAndWaiting) {
        return new ReservationAndWaitingResponse(
                reservationAndWaiting.id(),
                reservationAndWaiting.name(),
                reservationAndWaiting.date(),
                TimeResponse.from(reservationAndWaiting.timeSlot()),
                ThemeResponse.from(reservationAndWaiting.theme()),
                reservationAndWaiting.isReserved(),
                toWaitingNumber(reservationAndWaiting.waitingIndex())
        );
    }

    private static Integer toWaitingNumber(Integer waitingIndex) {
        if (waitingIndex == null) {
            return null;
        }
        return waitingIndex + 1;
    }
}
