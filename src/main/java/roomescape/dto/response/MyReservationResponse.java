package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;

public record MyReservationResponse(
        long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String orderId,
        Long amount,
        String paymentStatus,
        String paymentKey
) {
    public static MyReservationResponse from(Reservation reservation, PaymentOrder paymentOrder) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                paymentOrder.getOrderId(),
                paymentOrder.getAmount(),
                paymentOrder.getStatus().name(),
                paymentOrder.getPaymentKey()
        );
    }

    public static MyReservationResponse withoutPayment(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                null,
                null,
                PaymentStatus.CONFIRMED.name(),
                null
        );
    }
}
