package roomescape.domain.dto;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationsMineResponse(Long reservationId, String theme, LocalDate date, LocalTime time,
                                       String status) {
    public static ReservationsMineResponse from(Reservation reservation, Integer rank) {
        return new ReservationsMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getMessageWithRank(rank)
        );
    }
}
