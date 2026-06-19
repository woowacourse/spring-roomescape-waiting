package roomescape.payment;

public class PaymentAmountMismatchException extends RuntimeException {
    public PaymentAmountMismatchException(Long expected, Long actual) {
        super("결제 금액이 주문 금액과 다릅니다. 주문: " + expected + ", 요청: " + actual);
    }
}