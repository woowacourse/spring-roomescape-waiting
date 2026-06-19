package roomescape.global.exception;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException() {
        super("결제 정보를 찾을 수 없습니다. 다시 결제를 시도해주세요.");
    }
}
