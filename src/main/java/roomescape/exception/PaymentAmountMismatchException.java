package roomescape.exception;

public class PaymentAmountMismatchException extends RoomescapeException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super("AMOUNT_MISMATCH", "결제 금액과 저장된 금액이 일치하지 않습니다.\n 저장된 금액 : " + expected + ", 결제 요청 금액 : " + actual);
    }
}
