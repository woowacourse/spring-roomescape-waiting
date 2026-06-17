package roomescape.payment.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Order {
    private String orderId;
    private Long amount;
    private Long reservationId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String paymentKey;

    public static Order createPending(String orderId, Long amount, Long reservationId, Clock clock) {
        return Order.builder()
                .orderId(orderId)
                .amount(amount)
                .reservationId(reservationId)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    // 결제 승인 완료 시 상태 변경
    public Order complete(String paymentKey, Clock clock) {
        return Order.builder()
                .orderId(orderId)
                .amount(amount)
                .reservationId(reservationId)
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now(clock))
                .paymentKey(paymentKey)
                .build();
    }

    // 결제 실패 처리
    public Order fail(Clock clock) {
        return Order.builder()
                .orderId(orderId)
                .amount(amount)
                .reservationId(reservationId)
                .status(OrderStatus.FAILED)
                .createdAt(LocalDateTime.now(clock))
                .paymentKey(paymentKey)
                .build();
    }

    // 결제 취소 처리
    public Order cancel(Clock clock) {
        return Order.builder()
                .orderId(orderId)
                .amount(amount)
                .reservationId(reservationId)
                .status(OrderStatus.CANCELED)
                .createdAt(LocalDateTime.now(clock))
                .paymentKey(paymentKey)
                .build();
    }
}
