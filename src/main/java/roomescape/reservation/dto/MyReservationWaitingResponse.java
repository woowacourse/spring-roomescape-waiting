package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationWaitingResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        int order
) {
    public static MyReservationWaitingResponse from(final ReservationWaitingWithOrderDto reservationWaiting) {
        return new MyReservationWaitingResponse(
                reservationWaiting.id(),
                reservationWaiting.theme().name().getValue(),
                reservationWaiting.date().getValue(),
                reservationWaiting.time().startAt(),
                reservationWaiting.order()
        );
    }
}
