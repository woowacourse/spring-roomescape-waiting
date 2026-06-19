package roomescape.payment.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderStatus;
import roomescape.reservation.application.dto.ReservationIntegrationInfo;

@Builder
public record OrderInfo(
        String orderId,
        String paymentKey,
        Long amount,
        LocalDateTime createdAt,
        ReservationIntegrationInfo reservation,
        OrderStatus status
) {
    public static OrderInfo from(Order order, ReservationIntegrationInfo reservation) {
        return OrderInfo.builder()
                .orderId(order.getOrderId())
                .paymentKey(order.getPaymentKey())
                .amount(order.getAmount())
                .reservation(reservation)
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .build();
    }
}
