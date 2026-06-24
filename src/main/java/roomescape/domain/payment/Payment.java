package roomescape.domain.payment;

public class Payment {

    private final Long id;
    private final String paymentKey;
    private final String orderId;

    public Payment(Long id, String paymentKey, String orderId) {
        validatePaymentKey(paymentKey);
        validateOrderId(orderId);
        this.id = id;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
    }

    public Payment(String paymentKey, String orderId) {
        this(null, paymentKey, orderId);
    }

    public Long getId() {
        return id;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    private void validatePaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalArgumentException("결제 키는 필수입니다.");
        }
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다.");
        }
    }
}
