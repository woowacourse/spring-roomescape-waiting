package roomescape.payment.presentation.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.application.dto.OrderInfo;

@Builder
public record OrderResponse(
        String orderId,
        Long amount,
        Long reservationId,
        String themeName,
        LocalDateTime createdAt
) {
    public static OrderResponse from(OrderInfo order) {
        return OrderResponse.builder()
                .orderId(order.orderId())
                .amount(order.amount())
                .reservationId(order.reservationId())
                .themeName(order.themeName())
                .createdAt(order.createdAt())
                .build();
    }
}
