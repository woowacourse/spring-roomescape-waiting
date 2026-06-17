package roomescape.global.exception;

public class PaymentInvalidRequestException extends RoomEscapeException {

    public PaymentInvalidRequestException() {
        super("결제 요청이 올바르지 않거나 만료되었습니다. 다시 결제를 시도해주세요.");
    }
}
