package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationWaiting;

public record UserReservationServiceResponse(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status,
        int rank
) {

    // TODO : rank default값 처리하기
    public static UserReservationServiceResponse of(Reservation reservation) {
        return new UserReservationServiceResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().name(),
                -1
        );
    }

    public static UserReservationServiceResponse of(ReservationWaiting reservationWaiting, int rank) {
        return new UserReservationServiceResponse(
                reservationWaiting.getId(),
                reservationWaiting.getTheme().getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt(),
                reservationWaiting.getStatus().name(),
                rank
        );
    }
}
