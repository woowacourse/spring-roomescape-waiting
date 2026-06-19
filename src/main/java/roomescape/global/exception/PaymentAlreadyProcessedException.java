package roomescape.global.exception;

public class PaymentAlreadyProcessedException extends ConflictException {

    public PaymentAlreadyProcessedException() {
        super("이미 승인된 결제입니다.");
    }
}
