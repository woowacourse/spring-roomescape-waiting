package roomescape.common.exception;

/**
 * 나가는 호출이 자체 한도를 넘어, 외부로 보내지 않고 거부했음을 알리는 예외.
 */
public class OutboundRateLimitException extends RuntimeException {
    private String orderId;
    private Long amount;
    private String paymentKey;

    public OutboundRateLimitException(String message) {
        super(message);
    }

    public OutboundRateLimitException(String message, String orderId, Long amount, String paymentKey) {
        super(message);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
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
}
