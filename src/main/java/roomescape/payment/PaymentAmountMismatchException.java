package roomescape.payment;

public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super("결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
    }
}
