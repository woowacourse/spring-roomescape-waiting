package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationWithRank;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(ReservationWithRank reservation) {
        String status = "예약";
        if (reservation.isWaitingReservation()) {
            status = String.format("%d번째 예약대기", reservation.getRank());
        }
        return new MyReservationResponse(
                reservation.getReservationId(),
                reservation.getThemeName(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                status
        );
    }
}
