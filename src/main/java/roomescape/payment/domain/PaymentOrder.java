package roomescape.payment.domain;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentGatewayException;

public class PaymentOrder {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{6,64}$");

    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final Long amount;
    private final String paymentKey;
    private final PaymentOrderStatus status;
    private final LocalDateTime createdAt;

    public PaymentOrder(Long id, String orderId, Long reservationId, Long amount, String paymentKey,
                        PaymentOrderStatus status, LocalDateTime createdAt) {
        validateOrderId(orderId);
        validateReservationId(reservationId);
        validateAmount(amount);
        validateStatus(status);
        this.id = id;
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PaymentOrder ready(String orderId, Long reservationId, Long amount) {
        return new PaymentOrder(null, orderId, reservationId, amount, null, PaymentOrderStatus.READY, null);
    }

    public PaymentOrder complete(String approvedPaymentKey) {
        if (approvedPaymentKey == null || approvedPaymentKey.isBlank()) {
            throw new IllegalArgumentException("결제 키는 비어 있을 수 없습니다.");
        }
        return new PaymentOrder(id, orderId, reservationId, amount, approvedPaymentKey,
                PaymentOrderStatus.DONE, createdAt);
    }

    public PaymentOrder markUnconfirmed(String attemptedPaymentKey) {
        if (status != PaymentOrderStatus.READY) {
            throw new IllegalStateException("READY 상태의 결제 주문만 결과 불명확으로 표시할 수 있습니다.");
        }
        if (attemptedPaymentKey == null || attemptedPaymentKey.isBlank()) {
            throw new IllegalArgumentException("결제 키는 비어 있을 수 없습니다.");
        }
        return new PaymentOrder(id, orderId, reservationId, amount, attemptedPaymentKey,
                PaymentOrderStatus.UNCONFIRMED, createdAt);
    }

    public PaymentOrder confirmAfterRecovery(String approvedPaymentKey) {
        if (status != PaymentOrderStatus.UNCONFIRMED) {
            throw new IllegalStateException("결과 불명확 상태의 결제 주문만 회복 확정할 수 있습니다.");
        }
        if (approvedPaymentKey == null || approvedPaymentKey.isBlank()) {
            throw new IllegalArgumentException("결제 키는 비어 있을 수 없습니다.");
        }
        if (!paymentKey.equals(approvedPaymentKey)) {
            throw new IllegalStateException("결제 키가 일치하지 않습니다.");
        }
        return new PaymentOrder(id, orderId, reservationId, amount, approvedPaymentKey,
                PaymentOrderStatus.DONE, createdAt);
    }

    public PaymentOrder confirmWith(String approvedPaymentKey) {
        return isUnconfirmed()
                ? confirmAfterRecovery(approvedPaymentKey)
                : complete(approvedPaymentKey);
    }

    public void requireAmount(Long requested) {
        if (!this.amount.equals(requested)) {
            throw new PaymentAmountMismatchException();
        }
    }

    public void requireMatchingResult(String attemptedPaymentKey, PaymentResult result) {
        if (result == null
                || result.status() != PaymentStatus.DONE
                || !this.orderId.equals(result.orderId())
                || !this.amount.equals(result.approvedAmount())
                || !attemptedPaymentKey.equals(result.paymentKey())) {
            throw new PaymentGatewayException();
        }
    }

    public boolean isDone() {
        return status == PaymentOrderStatus.DONE;
    }

    public boolean isUnconfirmed() {
        return status == PaymentOrderStatus.UNCONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || !ORDER_ID_PATTERN.matcher(orderId).matches()) {
            throw new IllegalArgumentException("주문 ID는 6~64자의 영문, 숫자, -, _만 사용할 수 있습니다.");
        }
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null || reservationId <= 0) {
            throw new IllegalArgumentException("예약 ID는 양수여야 합니다.");
        }
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 양수여야 합니다.");
        }
    }

    private void validateStatus(PaymentOrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("결제 주문 상태는 비어 있을 수 없습니다.");
        }
    }
}
