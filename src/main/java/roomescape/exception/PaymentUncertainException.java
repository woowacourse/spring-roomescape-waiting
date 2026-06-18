package roomescape.exception;

public class PaymentUncertainException extends RuntimeException {

    public PaymentUncertainException() {
        super("결제 결과를 확인할 수 없습니다. 결제 내역을 확인해주세요.");
    }
}