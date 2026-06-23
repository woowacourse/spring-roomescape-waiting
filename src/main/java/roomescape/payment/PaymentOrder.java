package roomescape.payment;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

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

    public boolean isDone() {
        return status == PaymentOrderStatus.DONE;
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
