package roomescape.client;

public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super("결제 금액이 일치하지 않습니다. expected=" + expected + ", actual=" + actual);
    }
}
