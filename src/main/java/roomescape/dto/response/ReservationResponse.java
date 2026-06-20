package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;

public record ReservationResponse(
        long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String orderId,
        Long amount
) {
    public static ReservationResponse from(Reservation reservation, PaymentOrder paymentOrder) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                paymentOrder.getOrderId(),
                paymentOrder.getAmount()
        );
    }

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                null,
                null
        );
    }
}
