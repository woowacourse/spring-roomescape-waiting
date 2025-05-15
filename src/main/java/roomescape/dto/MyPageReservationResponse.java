package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationV2;

public record MyPageReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyPageReservationResponse from(ReservationV2 reservation) {
        return new MyPageReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }
}
