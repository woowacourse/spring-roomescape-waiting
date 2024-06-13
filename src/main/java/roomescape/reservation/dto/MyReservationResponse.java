package roomescape.reservation.dto;

import roomescape.reservation.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {
    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName().getValue(),
                reservation.getDate().getValue(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public static MyReservationResponse from(final WaitingWithRank waitingWithRank) {
        return new MyReservationResponse(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getReservation().getTheme().getName().getValue(),
                waitingWithRank.waiting().getReservation().getDate().getValue(),
                waitingWithRank.waiting().getReservation().getTime().getStartAt(),
                waitingWithRank.rank() + "번째 예약대기"
        );
    }
}
