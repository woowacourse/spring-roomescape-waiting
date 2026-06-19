package roomescape.controller.dto.response;

import roomescape.domain.Reservation;
import roomescape.service.dto.UserReservation;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        PaymentOrderResponse payment
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getSchedule().getDate(),
                ReservationTimeResponse.from(reservation.getSchedule().getTime()),
                ThemeResponse.from(reservation.getSchedule().getTheme()),
                null
        );
    }

    public static ReservationResponse from(UserReservation userReservation) {
        Reservation reservation = userReservation.reservation();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getSchedule().getDate(),
                ReservationTimeResponse.from(reservation.getSchedule().getTime()),
                ThemeResponse.from(reservation.getSchedule().getTheme()),
                PaymentOrderResponse.from(userReservation.payment())
        );
    }
}
