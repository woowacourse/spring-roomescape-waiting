package roomescape.exception;

public class PaymentAmountMismatchException extends RoomescapeBaseException {

    public PaymentAmountMismatchException() {
        super("결제 금액이 주문 금액과 일치하지 않습니다.");
    }
}
