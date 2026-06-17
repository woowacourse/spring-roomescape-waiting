package roomescape.payment.presentation.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.payment.domain.OrderStatus;

@Builder
public record OrderResponse(
        String orderId,
        Long amount,
        Long reservationId,
        String themeName,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponse from(OrderInfo order) {
        return OrderResponse.builder()
                .orderId(order.orderId())
                .amount(order.amount())
                .reservationId(order.reservationId())
                .themeName(order.themeName())
                .createdAt(order.createdAt())
                .status(order.status())
                .build();
    }
}
