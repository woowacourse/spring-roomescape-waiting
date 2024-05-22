package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.domain.Waiting;
import roomescape.entity.Reservation;

public record AdminReservationDetailResponse(
        long id,
        long waitingNumber,
        String member,
        String theme,
        LocalDate date,
        LocalTime time
) {
    public static AdminReservationDetailResponse from(Waiting waiting) {
        Reservation reservation = waiting.reservation();
        return new AdminReservationDetailResponse(
                reservation.getId(),
                waiting.rank(),
                reservation.getLoginMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt());
    }
}
