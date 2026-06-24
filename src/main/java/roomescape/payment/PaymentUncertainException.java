package roomescape.payment;

// read timeout — Toss가 이미 처리했을 수 있어 결제 성공/실패를 단정할 수 없다.
// 예약을 PAYMENT_UNCERTAIN으로 보존하고, 멱등키 덕에 재시도해도 이중 승인되지 않는다.
public class PaymentUncertainException extends RuntimeException {

    public PaymentUncertainException(String message) {
        super(message);
    }
}
