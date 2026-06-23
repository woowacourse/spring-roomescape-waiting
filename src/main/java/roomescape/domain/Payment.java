package roomescape.domain;

import java.util.UUID;

public class Payment {

    private final Long id;
    private final Long reservationId;
    private final String orderId;
    private final Long amount;
    private final String paymentKey;
    private final PaymentStatus status;
    private final String failureCode;
    private final String failureMessage;

    public Payment(Long id, Long reservationId, String orderId, Long amount, String paymentKey,
                   PaymentStatus status, String failureCode, String failureMessage) {
        validateReservationId(reservationId);
        validateOrderId(orderId);
        validateAmount(amount);
        validateStatus(status);

        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }

    public static Payment ready(Long reservationId, Long amount) {
        return new Payment(null, reservationId, generateOrderId(), amount, null, PaymentStatus.READY, null, null);
    }

    public Payment withId(Long id) {
        return new Payment(id, reservationId, orderId, amount, paymentKey, status, failureCode, failureMessage);
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    private static String generateOrderId() {
        return "payment_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null || reservationId <= 0) {
            throw new IllegalArgumentException("reservationId는 양수여야 합니다.");
        }
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || !orderId.matches("[A-Za-z0-9_-]{6,64}")) {
            throw new IllegalArgumentException("orderId 형식이 올바르지 않습니다.");
        }
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount는 양수여야 합니다.");
        }
    }

    private void validateStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status는 비어 있을 수 없습니다.");
        }
    }
}
