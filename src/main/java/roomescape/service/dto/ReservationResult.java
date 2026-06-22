package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.payment.Payment;
import roomescape.payment.PaymentOrderStatus;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        Long waitingOrder,
        String orderId,
        String paymentKey,
        Long amount,
        PaymentOrderStatus paymentStatus
) {
    public static ReservationResult from(ReservationWithWaitingOrder reservation) {
        return from(reservation, null);
    }

    public static ReservationResult from(ReservationWithWaitingOrder reservation, Payment payment) {
        return new ReservationResult(
                reservation.id(),
                reservation.name(),
                reservation.date(),
                ReservationTimeResult.from(reservation.time()),
                ThemeResult.from(reservation.theme()),
                reservation.waitingOrder(),
                payment == null ? null : payment.orderId(),
                payment == null ? null : payment.paymentKey(),
                payment == null ? null : payment.amount(),
                payment == null ? null : payment.status()
        );
    }
}
