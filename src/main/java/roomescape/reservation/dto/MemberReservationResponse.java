package roomescape.reservation.dto;

import roomescape.reservation.domain.entity.MemberReservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(Long id, String memberName, LocalDate date, LocalTime startAt, String themeName) {

    public static MemberReservationResponse from(MemberReservation memberReservation) {
        return new MemberReservationResponse(
                memberReservation.getId(),
                memberReservation.getMember().getName(),
                memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(),
                memberReservation.getReservation().getTheme().getName()
        );
    }
}
