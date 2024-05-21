package roomescape.service.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationStatus;

public record ReservationAppResponse(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse,
        ReservationStatus reservationStatus) {

    public static ReservationAppResponse from(Reservation reservation) {
        return new ReservationAppResponse(
                reservation.getId(),
                reservation.getMember().getName().getName(),
                reservation.getReservationDate(),
                new ReservationTimeAppResponse(
                        reservation.getReservationTime().getId(),
                        reservation.getReservationTime().getStartAt()),
                new ThemeAppResponse(reservation.getTheme().getId(),
                        reservation.getTheme().getName(),
                        reservation.getTheme().getDescription(),
                        reservation.getTheme().getThumbnail()),
                reservation.getStatus()
        );
    }
}
