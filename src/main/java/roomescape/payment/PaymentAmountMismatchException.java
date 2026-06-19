package roomescape.payment;

public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(long expectedAmount, long actualAmount) {
        super("결제 금액이 일치하지 않습니다. expected=" + expectedAmount + ", actual=" + actualAmount);
    }
}
