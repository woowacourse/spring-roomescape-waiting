package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationStatus;

public record MyReservationResponse(long reservationId, String themeName, LocalDate date, LocalTime time,
                                    String status) {
    public static MyReservationResponse from(MemberReservation memberReservation) {
        return new MyReservationResponse(memberReservation.getId(),
                memberReservation.getReservation().getTheme().getName(), memberReservation.getReservation().getDate(),
                memberReservation.getReservation().getTime().getStartAt(), ReservationStatus.BOOKED.getStatus());
    }
}
