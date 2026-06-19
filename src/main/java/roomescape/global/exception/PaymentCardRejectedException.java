package roomescape.global.exception;

public class PaymentCardRejectedException extends RoomEscapeException {

    public PaymentCardRejectedException() {
        super("카드 결제가 거절되었습니다. 다른 결제 수단으로 다시 시도해주세요.");
    }
}
