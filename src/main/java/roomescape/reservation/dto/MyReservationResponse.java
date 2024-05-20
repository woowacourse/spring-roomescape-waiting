package roomescape.reservation.dto;

import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyReservationResponse from(MemberReservation memberReservation) {
        return new MyReservationResponse(
                memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(),
                memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(),
                ReservationStatus.CONFIRMATION.getStatusName()
        );
    }
}
