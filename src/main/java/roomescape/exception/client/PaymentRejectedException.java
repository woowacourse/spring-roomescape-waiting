package roomescape.exception.client;

/**
 * 카드 거절 등, 사용자가 결제수단/정보를 바꿔 다시 시도해야 하는 결제 실패. (Toss: REJECT_CARD_PAYMENT 등 → 어댑터에서 이 예외로 번역. step2)
 */
public class PaymentRejectedException extends RoomEscapeClientException {

    public PaymentRejectedException(String message) {
        super(message);
    }
}
