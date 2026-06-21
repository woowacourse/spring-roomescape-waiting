package roomescape.payment.domain;

import roomescape.common.exception.UnprocessableContentException;

public class PaymentOrder {

    private final Long id;
    private final String orderId;
    private final int amount;
    private final String paymentKey;
    private final PaymentOrderStatus status;
    private final Long reservationId;

    private PaymentOrder(
            final Long id,
            final String orderId,
            final int amount,
            final String paymentKey,
            final PaymentOrderStatus status,
            final Long reservationId
    ) {
        validate(orderId, amount, reservationId);

        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
        this.reservationId = reservationId;
    }

    public static PaymentOrder create(final String orderId, final int amount, final Long reservationId) {
        return new PaymentOrder(null, orderId, amount, null, PaymentOrderStatus.READY, reservationId);
    }

    public static PaymentOrder of(
            final Long id,
            final String orderId,
            final int amount,
            final String paymentKey,
            final PaymentOrderStatus status,
            final Long reservationId
    ) {
        return new PaymentOrder(id, orderId, amount, paymentKey, status, reservationId);
    }

    public void validateSameAmount(final int requestedAmount) {
        if (amount != requestedAmount) {
            throw new UnprocessableContentException("결제 금액이 일치하지 않습니다.");
        }
    }

    public boolean isCompleted() {
        return status == PaymentOrderStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    private void validate(final String orderId, final int amount, final Long reservationId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 번호를 입력해야 합니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 양수여야 합니다.");
        }
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 id를 입력해야 합니다.");
        }
    }
}
