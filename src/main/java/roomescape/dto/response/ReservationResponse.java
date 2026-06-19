package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.payment.PaymentStatus;

public record ReservationResponse(
        Long id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime time,
        ReservationStatus reservationStatus,
        PaymentStatus paymentStatus,
        String orderId,
        String paymentKey,
        Long amount
) {
    public static ReservationResponse from(Reservation reservation) {
        return from(reservation, null);
    }

    public static ReservationResponse from(Reservation reservation, PaymentOrder paymentOrder) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus(),
                PaymentStatus.of(reservation.getStatus(), paymentOrder),
                paymentOrder == null ? null : paymentOrder.getOrderId(),
                paymentOrder == null ? null : paymentOrder.getPaymentKey(),
                paymentOrder == null ? null : paymentOrder.getAmount()
        );
    }
}
