package roomescape.reservation.domain;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
public class PaymentOrder {

    @With
    private final Long id;
    private final OrderId orderId;
    private final Long reservationId;
    private final PaymentAmount amount;
    private final String paymentKey;
    private final PaymentOrderStatus status;

    @Builder
    public PaymentOrder(Long id, OrderId orderId, Long reservationId, PaymentAmount amount, String paymentKey,
                        PaymentOrderStatus status) {
        this.id = id;
        this.orderId = Objects.requireNonNull(orderId);
        this.reservationId = Objects.requireNonNull(reservationId);
        this.amount = Objects.requireNonNull(amount);
        this.paymentKey = paymentKey;
        this.status = Objects.requireNonNull(status);
    }

    public static PaymentOrder create(Long reservationId, Long amount) {
        return PaymentOrder.builder()
                .orderId(OrderId.generate())
                .reservationId(reservationId)
                .amount(PaymentAmount.builder()
                        .value(amount)
                        .build())
                .status(PaymentOrderStatus.PENDING)
                .build();
    }

    public PaymentOrder confirm(String paymentKey) {
        return PaymentOrder.builder()
                .id(id)
                .orderId(orderId)
                .reservationId(reservationId)
                .amount(amount)
                .paymentKey(paymentKey)
                .status(PaymentOrderStatus.CONFIRMED)
                .build();
    }

    public boolean isPending() {
        return status == PaymentOrderStatus.PENDING && paymentKey == null;
    }
}
