package roomescape.reservation.dto;

import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.WaitingReservationRanking;

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
                memberReservation.getStatus().getStatusName()
        );
    }

    public static MyReservationResponse from(WaitingReservationRanking waitingReservationRanking) {
        MemberReservation memberReservation = waitingReservationRanking.getMemberReservation();
        String status = waitingReservationRanking.getDisplayRank() + "번째 " + memberReservation.getStatus().getStatusName();

        return new MyReservationResponse(
                memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(),
                memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(),
                status
        );
    }
}
