package roomescape.application.payment.exception;

/**
 * 주문 저장 금액과 요청 금액이 다를 때, confirm 호출 '전에' 차단하는 예외.
 */
public class PaymentAmountMismatchException extends RuntimeException {

    public PaymentAmountMismatchException(Long expected, Long actual) {
        super("결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
    }

}
