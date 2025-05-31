package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationsWithRankResponse(
        Long reservationId,
        String member,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        long rank) {
    public static ReservationsWithRankResponse from(ReservationWithRank reservationWithRank) {
        Reservation reservation = reservationWithRank.reservation();
        return new ReservationsWithRankResponse(
                reservation.getId(),
                reservation.getMember().getName().getValue(),
                reservation.getSlot().getTheme().getName(),
                reservation.getSlot().getDate(),
                reservation.getSlot().getTime().getStartAt(),
                reservation.getStatusValue(),
                reservationWithRank.rank());
    }
}
