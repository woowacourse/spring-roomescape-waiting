package roomescape.reservation.domain;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
public class Payment {

    @With
    private final Long id;
    private final OrderId orderId;
    private final Long reservationId;
    private final PaymentAmount amount;
    private final String paymentKey;
    private final PaymentStatus status;

    @Builder
    public Payment(Long id, OrderId orderId, Long reservationId, PaymentAmount amount, String paymentKey,
                   PaymentStatus status) {
        this.id = id;
        this.orderId = Objects.requireNonNull(orderId);
        this.reservationId = Objects.requireNonNull(reservationId);
        this.amount = Objects.requireNonNull(amount);
        this.paymentKey = paymentKey;
        this.status = Objects.requireNonNull(status);
    }

    public static Payment create(Long reservationId, Long amount) {
        return Payment.builder()
                .orderId(OrderId.generate())
                .reservationId(reservationId)
                .amount(PaymentAmount.builder()
                        .value(amount)
                        .build())
                .status(PaymentStatus.PENDING)
                .build();
    }

    public Payment confirm(String paymentKey) {
        return Payment.builder()
                .id(id)
                .orderId(orderId)
                .reservationId(reservationId)
                .amount(amount)
                .paymentKey(paymentKey)
                .status(PaymentStatus.CONFIRMED)
                .build();
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING && paymentKey == null;
    }
}
