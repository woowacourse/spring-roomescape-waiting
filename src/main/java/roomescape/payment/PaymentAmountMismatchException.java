package roomescape.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 주문 저장 금액과 요청 금액이 다를 때, confirm 호출 '전에' 차단하는 예외.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentAmountMismatchException extends RuntimeException {

  public PaymentAmountMismatchException(Long expected, Long actual) {
    super("결제 금액이 일치하지 않습니다. expected=" + expected + ", actual=" + actual);
  }

}
