package roomescape.reservation.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
public class PaymentOrder {

    @With
    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final int amount;
    private final String paymentKey;
    private final PaymentStatus status;

    @Builder
    public PaymentOrder(Long id, String orderId, Long reservationId, int amount, String paymentKey,
                        PaymentStatus status) {
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
    }
}
