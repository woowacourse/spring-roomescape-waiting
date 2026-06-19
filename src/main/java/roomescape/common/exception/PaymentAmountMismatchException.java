package roomescape.common.exception;

/**
 * 주문 저장 금액과 요청 금액이 다를 때, confirm 호출 '전에' 차단하는 예외.
 */
public class PaymentAmountMismatchException extends RuntimeException {
    private final String orderId;
    private final Long amount;
    private final String paymentKey;

    public PaymentAmountMismatchException(Long expected, Long actual, String orderId, Long amount, String paymentKey) {
        super("결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
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
