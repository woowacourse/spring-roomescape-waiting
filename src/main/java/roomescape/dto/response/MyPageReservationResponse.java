package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MyPageReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyPageReservationResponse from(Reservation reservation) {
        return new MyPageReservationResponse(
                reservation.getId(),
                reservation.getReservationItem().getTheme().getName(),
                reservation.getReservationItem().getDate(),
                reservation.getReservationItem().getTime().getStartAt(),
                "예약"
        );
    }
}
