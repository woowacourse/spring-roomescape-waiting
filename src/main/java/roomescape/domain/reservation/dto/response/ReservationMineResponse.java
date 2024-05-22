package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.dto.ReservationWithOrderDto;

public record ReservationMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {

    public static ReservationMineResponse from(ReservationWithOrderDto reservationWithOrderDto) {
        if (reservationWithOrderDto.orderNumber() == 0) {
            return new ReservationMineResponse(reservationWithOrderDto.getId(),
                    reservationWithOrderDto.getTheme().getName(), reservationWithOrderDto.getDate(),
                    reservationWithOrderDto.getTime().getStartAt(), "예약");
        }
        String status = reservationWithOrderDto.orderNumber() + "번째 예약대기";
        return new ReservationMineResponse(reservationWithOrderDto.getId(),
                reservationWithOrderDto.getTheme().getName(), reservationWithOrderDto.getDate(),
                reservationWithOrderDto.getTime().getStartAt(), status);
    }
}
