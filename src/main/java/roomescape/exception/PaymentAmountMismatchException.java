package roomescape.exception;

public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super("결제 금액이 일치하지 않습니다. 예상: %d, 실제: %d".formatted(expected, actual));
    }
}