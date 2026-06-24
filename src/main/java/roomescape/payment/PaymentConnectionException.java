package roomescape.payment;

// 연결 단계에서 실패 — Toss 서버에 요청이 도달하지 않았으므로 결제 취소 후 재시도 안내.
public class PaymentConnectionException extends RuntimeException {

    public PaymentConnectionException(String message) {
        super(message);
    }
}
