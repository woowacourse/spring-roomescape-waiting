package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaiting;

public record MemberReservationStatusResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status) {

    public MemberReservationStatusResponse(Reservation reservation) {
        this(reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getValue()
        );
    }

    public MemberReservationStatusResponse(ReservationWaiting reservationWaiting) {
        this(reservationWaiting.getId(),
                reservationWaiting.getTheme().getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt(),
                reservationWaiting.getRank() + "번째 " + reservationWaiting.getStatus().getValue()
        );
    }
}
