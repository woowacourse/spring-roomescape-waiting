package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.dto.ReservationWithOrderDto;

public record ReservationMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {

    public static ReservationMineResponse from(ReservationWithOrderDto reservationWithOrderDto) {
        Reservation reservation = reservationWithOrderDto.reservation();
        if (reservationWithOrderDto.orderNumber() == 0) {
            return new ReservationMineResponse(
                    reservation.getId(),
                    reservation.getTheme().getName(),
                    reservation.getDate(),
                    reservation.getTime().getStartAt(),
                    "예약");
        }
        String status = reservationWithOrderDto.orderNumber() + "번째 예약대기";
        return new ReservationMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status);
    }
}
