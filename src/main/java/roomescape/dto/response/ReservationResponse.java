package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public record ReservationResponse(
        Long id,
        LocalDate date,
        String name,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        String theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getMember().getName(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getRawName()
        );
    }

    public static ReservationResponse from(ReservationWaiting reservationWaiting) { // todo 디미터
        return new ReservationResponse(
                reservationWaiting.getId(),
                reservationWaiting.getReservation().getDate(),
                reservationWaiting.getMember().getName(),
                reservationWaiting.getReservation().getTime().getStartAt(),
                reservationWaiting.getReservation().getTheme().getRawName()
        );
    }
}
