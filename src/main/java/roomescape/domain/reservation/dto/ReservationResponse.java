package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.payment.ReservationPayment;
import roomescape.domain.reservation.Reservation;

public record ReservationResponse(
    Long id,
    String name,
    LocalDate date,
    Long timeId,
    Long themeId,
    String orderId,
    Long amount
) {

    public static ReservationResponse from(Reservation reservation, ReservationPayment payment) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId(),
            payment.orderId(),
            payment.amount()
        );
    }
}
