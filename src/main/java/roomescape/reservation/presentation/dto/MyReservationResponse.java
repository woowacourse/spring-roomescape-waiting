package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

public record MyReservationResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            "예약"
        );
    }

    public static MyReservationResponse from(final WaitingReservationWithRank waitingReservationWithRank) {
        return new MyReservationResponse(
            waitingReservationWithRank.waitingReservation().getId(),
            waitingReservationWithRank.waitingReservation().getTheme().getName(),
            waitingReservationWithRank.waitingReservation().getDate(),
            waitingReservationWithRank.waitingReservation().getTime().getStartAt(),
            (waitingReservationWithRank.rank()+1) + "번째 예약대기"
        );
    }
}
