package roomescape.exception;

public class PaymentAmountMismatchException extends RoomescapeException {

    private static final String MESSAGE_FORMAT = "결제 금액이 일치하지 않습니다. 주문 금액=%d, 요청 금액=%d";

    public PaymentAmountMismatchException(long expected, long actual) {
        super(MESSAGE_FORMAT.formatted(expected, actual));
    }
}
