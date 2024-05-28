package roomescape.reservation.dto;

import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.Reservation;
import roomescape.reservation.domain.dto.WaitingReservationRanking;

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
        Reservation reservation = memberReservation.getReservation();

        return new MyReservationResponse(
                memberReservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                memberReservation.getStatus().getStatusName()
        );
    }

    public static MyReservationResponse from(WaitingReservationRanking waitingReservationRanking) {
        MemberReservation memberReservation = waitingReservationRanking.getMemberReservation();
        String status = waitingReservationRanking.getDisplayRank() + "번째 " + memberReservation.getStatus().getStatusName();
        Reservation reservation = memberReservation.getReservation();

        return new MyReservationResponse(
                memberReservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status
        );
    }
}
