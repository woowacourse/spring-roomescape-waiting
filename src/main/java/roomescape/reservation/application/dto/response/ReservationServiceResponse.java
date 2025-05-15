package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.dto.ReservationWithMember;

public record ReservationServiceResponse(
    Long id,
    String name,
    LocalDate date,
    LocalTime startAt,
    String themeName
) {

    public static ReservationServiceResponse from(Reservation reservation, String name) {
        return new ReservationServiceResponse(
            reservation.getId(),
            name,
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            reservation.getTheme().getName()
        );
    }

    public static ReservationServiceResponse from(ReservationWithMember reservationWithMember) {
        return new ReservationServiceResponse(
            reservationWithMember.id(),
            reservationWithMember.memberName(),
            reservationWithMember.date(),
            reservationWithMember.startAt(),
            reservationWithMember.themeName()
        );
    }
}
