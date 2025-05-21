package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.vo.ReservationStatus;

public record UserReservationServiceResponse(
        Long reservationId,
        String themeName,
        LocalDate date,
        LocalTime time,
        ReservationStatus status
) {

    public static UserReservationServiceResponse from(Reservation reservation, ReservationStatus reservationStatus) {
        return new UserReservationServiceResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservationStatus
        );
    }
}
