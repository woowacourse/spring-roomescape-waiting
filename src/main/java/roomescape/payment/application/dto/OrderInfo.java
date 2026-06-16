package roomescape.payment.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderStatus;
import roomescape.reservation.domain.ActiveReservation;

@Builder
public record OrderInfo(
        String orderId,
        Long amount,
        Long reservationId,
        String username,
        String themeName,
        LocalDateTime createdAt,
        OrderStatus status
) {
    public static OrderInfo from(Order order) {
        ActiveReservation reservation = order.getReservation();
        return OrderInfo.builder()
                .orderId(order.getOrderId())
                .amount(order.getAmount())
                .reservationId(reservation.getId())
                .username(order.getReservation().getName())
                .themeName(reservation.themeName())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .build();
    }
}
