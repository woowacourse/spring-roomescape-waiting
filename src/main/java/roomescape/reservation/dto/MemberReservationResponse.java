package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        Long sequence
) {

    public static MemberReservationResponse of(Reservation reservation, Long sequence) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getStatusName(),
                sequence
        );
    }
}
