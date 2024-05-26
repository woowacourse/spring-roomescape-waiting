package roomescape.application.dto.response.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWithRank;

public record UserReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static UserReservationResponse from(Reservation reservation) {
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getDetail().getTheme().getName(),
                reservation.getDetail().getDate(),
                reservation.getDetail().getTime().getStartAt(),
                "예약");
    }

    public static UserReservationResponse from(ReservationWithRank reservationWithRank) {
        Reservation reservation = reservationWithRank.reservation();
        long rank = reservationWithRank.rank();
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getDetail().getTheme().getName(),
                reservation.getDetail().getDate(),
                reservation.getDetail().getTime().getStartAt(),
                String.format("%d번째 예약 대기", rank));
    }
}
