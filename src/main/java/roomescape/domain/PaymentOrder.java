package roomescape.domain;

import java.util.Objects;

public class PaymentOrder {

    private final Long id;
    private final String orderId;
    private final long amount;
    private final PaymentStatus status;
    private final Long reservationId;
    private final String paymentKey;

    public static PaymentOrder createPending(String orderId, long amount, Reservation reservation) {
        return new PaymentOrder(null, orderId, amount, PaymentStatus.PAYMENT_PENDING, reservation.getId(), null);
    }

    public PaymentOrder(Long id, String orderId, long amount, PaymentStatus status,
                        Long reservationId, String paymentKey) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.reservationId = reservationId;
        this.paymentKey = paymentKey;
    }

    public PaymentOrder createWithId(long id) {
        return new PaymentOrder(id, this.orderId, this.amount, this.status, this.reservationId, this.paymentKey);
    }

    public PaymentOrder confirm(String paymentKey) {
        return new PaymentOrder(this.id, this.orderId, this.amount, PaymentStatus.CONFIRMED, this.reservationId,
                paymentKey);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentOrder that)) {
            return false;
        }
        if (id == null) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
