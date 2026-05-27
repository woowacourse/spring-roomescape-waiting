package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationtime.dto.response.CreateReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;

import java.time.LocalDate;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        CreateReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Integer order
) {
    public static MyReservationResponse fromReservation(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                new CreateReservationTimeResponse(
                        reservation.getTime().getId(),
                        reservation.getTime().getStartAt()
                ),
                new ThemeResponse(
                        reservation.getTheme().getId(),
                        reservation.getTheme().getName(),
                        reservation.getTheme().getDescription(),
                        reservation.getTheme().getThumbnail()
                ),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static MyReservationResponse fromReservationWaiting(ReservationWaiting reservationWaiting, int order) {
        return new MyReservationResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getReservationDate(),
                new CreateReservationTimeResponse(
                        reservationWaiting.getTime().getId(),
                        reservationWaiting.getTime().getStartAt()
                ),
                new ThemeResponse(
                        reservationWaiting.getTheme().getId(),
                        reservationWaiting.getTheme().getName(),
                        reservationWaiting.getTheme().getDescription(),
                        reservationWaiting.getTheme().getThumbnail()
                ),
                ReservationStatus.WAITING,
                order
        );
    }
}
