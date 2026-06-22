package roomescape.reservation.controller.dto.response;

import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.controller.dto.response.ReservationTimeResponse;
import roomescape.theme.controller.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.Optional;

public record ReservationPaymentResponse(
        Long id,
        String name,
        String email,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String reservationStatus,
        String paymentStatus,
        String paymentStatusLabel,
        String orderId,
        String paymentKey,
        Integer amount
) {

    public static ReservationPaymentResponse from(
            final Reservation reservation,
            final Optional<PaymentOrder> paymentOrder
    ) {
        return new ReservationPaymentResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getCustomerEmail(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().name(),
                paymentOrder.map(order -> order.getStatus().name())
                        .orElse(null),
                paymentOrder.map(order -> labelOf(order.getStatus()))
                        .orElse(labelOfReservation(reservation)),
                paymentOrder.map(PaymentOrder::getOrderId)
                        .orElse(null),
                paymentOrder.map(PaymentOrder::getPaymentKey)
                        .orElse(null),
                paymentOrder.map(PaymentOrder::getAmount)
                        .orElse(null)
        );
    }

    private static String labelOf(final PaymentOrderStatus status) {
        return switch (status) {
            case READY -> "결제 대기";
            case REQUIRES_CONFIRMATION -> "확인 필요";
            case COMPLETED -> "확정";
        };
    }

    private static String labelOfReservation(final Reservation reservation) {
        if (reservation.isConfirmed()) {
            return "확정";
        }

        return "결제 정보 없음";
    }
}
