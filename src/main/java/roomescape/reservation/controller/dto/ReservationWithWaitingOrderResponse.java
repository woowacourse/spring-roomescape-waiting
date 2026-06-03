package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.MyReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationWithWaitingOrderResponse(
        Long id,
        String name,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Status status,
        Integer waitingOrder
) {
    public static ReservationWithWaitingOrderResponse from(MyReservation myReservation) {
        Reservation reservation = myReservation.reservation();
        return new ReservationWithWaitingOrderResponse(
                reservation.getId(),
                reservation.getName(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus(),
                myReservation.waitingOrder()
        );
    }
}
