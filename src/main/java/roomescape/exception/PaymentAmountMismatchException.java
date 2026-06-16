package roomescape.exception;

/**
 * 결제 승인 요청 금액이 서버의 주문 금액과 다를 때 발생하는 예외. 금액 위변조 시도를 차단한다.
 */
public class PaymentAmountMismatchException extends RuntimeException {

  public PaymentAmountMismatchException(Long expected, Long actual) {
    super(String.format("결제 금액이 일치하지 않습니다. (주문금액: %d, 요청금액: %d)", expected, actual));
  }

}
