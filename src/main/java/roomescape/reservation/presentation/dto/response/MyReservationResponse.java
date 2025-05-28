package roomescape.reservation.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.infrastructure.dto.ReservationWithRank;

public record MyReservationResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {
    public static MyReservationResponse from(ReservationWithRank reservation) {
        String status = findStatus(reservation);
        return new MyReservationResponse(reservation.getReservationId(), reservation.getThemeName(),
                reservation.getReservationDate(), reservation.getReservationTime(), status);
    }

    private static String findStatus(ReservationWithRank reservation) {
        if (reservation.isWaitingReservation()) {
            return String.format("%d번째 예약대기", reservation.getRank());
        }
        return ReservationStatus.RESERVED.getStatus();
    }
}
