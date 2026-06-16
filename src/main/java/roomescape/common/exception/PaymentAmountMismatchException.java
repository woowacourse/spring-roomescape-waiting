package roomescape.common.exception;

/**
 * 콜백으로 넘어온 결제 금액이 주문에 저장해 둔 금액과 다를 때. 승인(confirm) 호출 전에 서버가 직접 차단한다.
 * 토스엔 금액 불일치 전용 코드가 없어 우리가 검증하는 도메인 예외다. → 400
 */
public class PaymentAmountMismatchException extends DomainException {
    public PaymentAmountMismatchException(String message) {
        super(message);
    }
}
