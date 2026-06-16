package roomescape.exception.client;

/**
 * 같은 결제가 이미 승인된 경우(Toss: ALREADY_PROCESSED_PAYMENT).
 * <p>
 * [열린 결정] '에러'로 볼지 '이미 성공'으로 볼지는 아직 결정하지 않는다. 우선 클라이언트 예외로 분류해 두고, mission step2(멱등키) 도입 시 성공 처리로 흡수할지 재검토한다.
 */
public class PaymentAlreadyProcessedException extends RoomEscapeClientException {

    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }
}
