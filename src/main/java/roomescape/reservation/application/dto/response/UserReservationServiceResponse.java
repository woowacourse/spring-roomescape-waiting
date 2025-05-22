package roomescape.reservation.application.dto.response;

import static roomescape.reservation.model.vo.ReservationStatus.*;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.vo.ReservationStatus;

public record UserReservationServiceResponse(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        ReservationStatus status,
        int rank
) {

    // TODO : rank default값 처리하기
    public static UserReservationServiceResponse of(Reservation reservation, ReservationStatus reservationStatus) {
        return new UserReservationServiceResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservationStatus,
                -1
        );
    }

    public static UserReservationServiceResponse of(ReservationWaiting reservationWaiting, int rank) {
        return new UserReservationServiceResponse(
                reservationWaiting.getId(),
                reservationWaiting.getTheme().getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt(),
                WAITING,
                rank
        );
    }
}
