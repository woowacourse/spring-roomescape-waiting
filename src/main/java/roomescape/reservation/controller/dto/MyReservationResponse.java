package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.MemberReservation;
// TODO reservationId 보다는 memberReservationId 가 맞지 않나?
public record MyReservationResponse(long reservationId, String themeName, LocalDate date, LocalTime time,
                                    String status) {
    public static MyReservationResponse from(MemberReservation memberReservation) {
        return new MyReservationResponse(memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(), memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(), memberReservation.getStatus().getStatus());
    }

    public static MyReservationResponse from(MyReservationWithStatus myReservationWithStatus) {
        return new MyReservationResponse(
                myReservationWithStatus.memberReservationId(),
                myReservationWithStatus.themeName(),
                myReservationWithStatus.date(),
                myReservationWithStatus.time(),
                myReservationWithStatus.status().getStatus()
        );
    }
}
