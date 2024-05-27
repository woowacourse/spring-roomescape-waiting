package roomescape.reservation.dto;

import roomescape.reservation.domain.entity.MemberReservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record AdminWaitingReservationResponse(
        Long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime time
) {

    public static AdminWaitingReservationResponse from(MemberReservation memberReservation) {
        return new AdminWaitingReservationResponse(
                memberReservation.getId(),
                memberReservation.getMember().getName(),
                memberReservation.getReservation().getTheme().getName(),
                memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt()
        );
    }
}
