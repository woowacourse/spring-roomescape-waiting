package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.MemberReservation;

public record MyReservationResponse(long reservationId, String themeName, LocalDate date, LocalTime time,
                                    String status) {
    public static MyReservationResponse from(MemberReservation memberReservation) {
        return new MyReservationResponse(memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(), memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(), memberReservation.getStatus().getStatus());
    }

    public static MyReservationResponse from(MyReservationWithStatus myReservationWithStatus) {
        if (myReservationWithStatus.status().isWaiting()) {
            return new MyReservationResponse(
                    myReservationWithStatus.reservationId(),
                    myReservationWithStatus.themeName(),
                    myReservationWithStatus.date(),
                    myReservationWithStatus.time(),
                    myReservationWithStatus.waitingOrder() + "번째 " + myReservationWithStatus.status().getStatus()
            );
        }
        return new MyReservationResponse(
                myReservationWithStatus.reservationId(),
                myReservationWithStatus.themeName(),
                myReservationWithStatus.date(),
                myReservationWithStatus.time(),
                myReservationWithStatus.status().getStatus()
        );
    }
}
