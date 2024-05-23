package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(long reservationId, String themeName, LocalDate date, LocalTime time,
                                    String status) {
    public static MyReservationResponse from(MemberReservation memberReservation) {
        Reservation reservation = memberReservation.getReservation();
        return new MyReservationResponse(
                memberReservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                memberReservation.getStatus().getStatus());
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

    public static MyReservationResponse from(MyReservationWithStatus myReservationWithStatus, int waitingCount) {
        return new MyReservationResponse(
                myReservationWithStatus.memberReservationId(),
                myReservationWithStatus.themeName(),
                myReservationWithStatus.date(),
                myReservationWithStatus.time(),
                waitingCount + "번째 " + myReservationWithStatus.status().getStatus()
        );
    }
}
