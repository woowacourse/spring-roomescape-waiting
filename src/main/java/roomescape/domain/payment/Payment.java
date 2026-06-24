package roomescape.domain.payment;

public class Payment {

    private final Long id;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;
    private final Long reservationId;

    public Payment(Long id, String orderId, String paymentKey, Long amount, Long reservationId) {
        validateOrderId(orderId);
        validateAmount(amount);
        validateReservationId(reservationId);
        this.id = id;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.reservationId = reservationId;
    }

    public Payment(String orderId, Long amount, Long reservationId) {
        this(null, orderId, null, amount, reservationId);
    }

    public Long getId() {
        return id;
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

    public Long getReservationId() {
        return reservationId;
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다.");
        }
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("결제 금액은 0 이상이어야 합니다.");
        }
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 식별자는 필수입니다.");
        }
    }
}
