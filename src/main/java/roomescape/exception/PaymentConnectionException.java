package roomescape.exception;

public class PaymentConnectionException extends RuntimeException {

    public PaymentConnectionException() {
        super("결제 서버에 연결할 수 없습니다.");
    }
}