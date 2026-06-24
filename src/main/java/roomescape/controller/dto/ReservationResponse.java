package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.service.dto.ReservationCreateResult;

public record ReservationResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        String orderId,
        Long amount
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTimeSlot()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus(),
                null,
                null
        );
    }

    public static ReservationResponse from(ReservationCreateResult result) {
        Reservation reservation = result.reservation();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTimeSlot()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus(),
                result.orderId(),
                result.amount()
        );
    }
}
