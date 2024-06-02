package roomescape.service.dto.response.wait;

import static roomescape.domain.ReservationStatus.Status.RESERVED;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.domain.Reservation;

public record ReservationWithStatusResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status) {

    private ReservationWithStatusResponse(Reservation reservation, String statusText) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                statusText
        );
    }

    public static ReservationWithStatusResponse from(Reservation reservation, long rank) {
        if (reservation.getStatus().isSameAs(RESERVED)) {
            return new ReservationWithStatusResponse(reservation, "예약");
        }
        return new ReservationWithStatusResponse(reservation, rank + "번째 예약대기");
    }
}
