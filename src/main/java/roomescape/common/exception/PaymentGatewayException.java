package roomescape.common.exception;

public class PaymentGatewayException extends RuntimeException {
    private final String orderId;
    private final Long amount;
    private final String paymentKey;

    public PaymentGatewayException(String message, String orderId, Long amount, String paymentKey, Throwable cause) {
        super(message, cause);
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
