package roomescape.payment;

public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(Long expectedAmount, Long actualAmount) {
        super("결제 금액이 일치하지 않습니다. expected=%d, actual=%d".formatted(expectedAmount, actualAmount));
    }
}
