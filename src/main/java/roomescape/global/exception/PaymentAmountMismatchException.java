package roomescape.global.exception;

public class PaymentAmountMismatchException extends RoomEscapeException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super(String.format("결제 금액이 일치하지 않습니다. 저장된 금액: %d, 요청 금액: %d", expected, actual));
    }
}
