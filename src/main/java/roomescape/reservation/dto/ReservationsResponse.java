package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationsResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        long rank) {
    public static ReservationsResponse from(ReservationWithRank reservationWithRank) {
        Reservation reservation = reservationWithRank.reservation();
        return new ReservationsResponse(
                reservation.getId(),
                reservation.getSlot().getTheme().getName(),
                reservation.getSlot().getDate(),
                reservation.getSlot().getTime().getStartAt(),
                reservation.getStatusValue(),
                reservationWithRank.rank());
    }
}
