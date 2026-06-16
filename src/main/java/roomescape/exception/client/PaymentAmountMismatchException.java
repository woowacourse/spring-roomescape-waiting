package roomescape.exception.client;

/**
 * 주문 시 저장한 금액과 결제 콜백으로 넘어온 금액이 다를 때. 승인 API 호출 '전에' 서버가 직접 차단한다(토스엔 금액 불일치 코드가 없음).
 */
public class PaymentAmountMismatchException extends RoomEscapeClientException {

    public PaymentAmountMismatchException(String message) {
        super(message);
    }
}
