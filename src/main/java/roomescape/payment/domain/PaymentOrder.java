package roomescape.payment.domain;

import roomescape.common.exception.UnprocessableContentException;

import java.util.UUID;

public class PaymentOrder {

    private final Long id;
    private final String orderId;
    private final int amount;
    private final String paymentKey;
    private final String idempotencyKey;
    private final PaymentOrderStatus status;
    private final Long reservationId;

    private PaymentOrder(
            final Long id,
            final String orderId,
            final int amount,
            final String paymentKey,
            final String idempotencyKey,
            final PaymentOrderStatus status,
            final Long reservationId
    ) {
        validate(orderId, amount, idempotencyKey, reservationId);

        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.reservationId = reservationId;
    }

    public static PaymentOrder create(final String orderId, final int amount, final Long reservationId) {
        return new PaymentOrder(
                null,
                orderId,
                amount,
                null,
                UUID.randomUUID().toString(),
                PaymentOrderStatus.READY,
                reservationId
        );
    }

    public static PaymentOrder of(
            final Long id,
            final String orderId,
            final int amount,
            final String paymentKey,
            final String idempotencyKey,
            final PaymentOrderStatus status,
            final Long reservationId
    ) {
        return new PaymentOrder(id, orderId, amount, paymentKey, idempotencyKey, status, reservationId);
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    private void validate(
            final String orderId,
            final int amount,
            final String idempotencyKey,
            final Long reservationId
    ) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 번호를 입력해야 합니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 양수여야 합니다.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 300) {
            throw new IllegalArgumentException("유효한 멱등키를 입력해야 합니다.");
        }
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 id를 입력해야 합니다.");
        }
    }
}
