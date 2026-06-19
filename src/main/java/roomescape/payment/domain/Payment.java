package roomescape.payment.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {

    private final Long id;
    private final Long reservationId;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;
    private final PaymentState state;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Payment(
            Long id,
            Long reservationId,
            String orderId,
            String paymentKey,
            Long amount,
            PaymentState state,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Payment pending(Long reservationId, String orderId, Long amount, LocalDateTime now) {
        return new Payment(null, reservationId, orderId, null, amount, PaymentState.PENDING, now, now);
    }

    public Payment confirm(String paymentKey, LocalDateTime now) {
        return new Payment(id, reservationId, orderId, paymentKey, amount, PaymentState.CONFIRMED, createdAt, now);
    }

    public Payment cancel(LocalDateTime now) {
        return new Payment(id, reservationId, orderId, paymentKey, amount, PaymentState.CANCELED, createdAt, now);
    }

    public boolean isConfirmed() {
        return state == PaymentState.CONFIRMED;
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

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentState getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
