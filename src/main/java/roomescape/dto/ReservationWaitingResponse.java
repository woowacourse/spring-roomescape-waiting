package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        int order
) {
    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting) {
        Reservation reservation = reservationWaiting.getReservation();
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservationWaiting.getOrder()
        );
    }
}
