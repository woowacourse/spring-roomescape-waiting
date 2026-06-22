package roomescape.exception.server;

/**
 * 토스 승인 호출이 read timeout 등으로 결과가 불확실한 상태. '실패'가 아니라 '확인 필요'다. 토스에선 이미 승인됐을 수 있어, 예약/결제를 절대 삭제하지 않는다. 안전한 재시도는
 * 멱등키(orderId)로 보장된다.
 */
public class PaymentTimeoutException extends RoomEscapeServerException {
    public PaymentTimeoutException(String message) {
        super(message);
    }
}
