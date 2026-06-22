package roomescape.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentConnectionFailedException extends PaymentFailureException {

    public PaymentConnectionFailedException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "CONNECTION_FAILED", "토스 결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }
}
