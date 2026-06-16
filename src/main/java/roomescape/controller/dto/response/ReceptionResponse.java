package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Wait;

public record ReceptionResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        Long order,
        String orderId,
        Long amount
) {
    public static ReceptionResponse from(Reservation reservation, Long order, String status) {
        return new ReceptionResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                status,
                order,
                reservation.getOrderId(),
                reservation.getAmount()
        );
    }

    public static ReceptionResponse from(Wait wait, Long order, String status) {
        return new ReceptionResponse(
                wait.getId(),
                wait.getName(),
                wait.getReservationDate(),
                ReservationTimeResponse.from(wait.getTime()),
                ThemeResponse.from(wait.getTheme()),
                status,
                order,
                null,
                null
        );
    }
}
