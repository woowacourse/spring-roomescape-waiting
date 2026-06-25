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

    private Payment(Long id, Long reservationId, String orderId, Long amount, String paymentKey,
                    PaymentStatus status, String failureCode, String failureMessage) {
        validateReservationId(reservationId);
        validateOrderId(orderId);
        validateAmount(amount);
        validateStatus(status);
        validateStatusFields(status, paymentKey, failureCode);

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

    public static Payment restore(Long id, Long reservationId, String orderId, Long amount, String paymentKey,
                                  PaymentStatus status, String failureCode, String failureMessage) {
        validateId(id);
        return new Payment(id, reservationId, orderId, amount, paymentKey, status, failureCode, failureMessage);
    }

    public Payment withId(Long id) {
        validateId(id);
        return new Payment(id, reservationId, orderId, amount, paymentKey, status, failureCode, failureMessage);
    }

    public Payment confirm(String paymentKey) {
        if (status != PaymentStatus.READY) {
            throw new IllegalStateException("결제 대기 상태에서만 승인할 수 있습니다.");
        }
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalArgumentException("paymentKey는 비어 있을 수 없습니다.");
        }
        return new Payment(id, reservationId, orderId, amount, paymentKey, PaymentStatus.CONFIRMED,
                failureCode, failureMessage);
    }

    public Payment fail(String failureCode, String failureMessage) {
        if (status != PaymentStatus.READY) {
            throw new IllegalStateException("결제 대기 상태에서만 실패 처리할 수 있습니다.");
        }
        PaymentStatus failedStatus = "PAY_PROCESS_CANCELED".equals(failureCode)
                ? PaymentStatus.CANCELED
                : PaymentStatus.FAILED;
        return new Payment(id, reservationId, orderId, amount, paymentKey, failedStatus,
                failureCode, failureMessage);
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

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id는 양수여야 합니다.");
        }
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

    private void validateStatusFields(PaymentStatus status, String paymentKey, String failureCode) {
        if (status == PaymentStatus.READY && (paymentKey != null || failureCode != null)) {
            throw new IllegalArgumentException("결제 대기 상태는 paymentKey나 실패 코드를 가질 수 없습니다.");
        }
        if (status == PaymentStatus.CONFIRMED && (paymentKey == null || paymentKey.isBlank())) {
            throw new IllegalArgumentException("승인된 결제는 paymentKey가 필요합니다.");
        }
        if ((status == PaymentStatus.FAILED || status == PaymentStatus.CANCELED)
                && (failureCode == null || failureCode.isBlank())) {
            throw new IllegalArgumentException("실패 또는 취소된 결제는 실패 코드가 필요합니다.");
        }
    }
}
