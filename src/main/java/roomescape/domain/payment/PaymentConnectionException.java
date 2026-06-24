package roomescape.domain.payment;

import org.springframework.http.HttpStatus;

public class PaymentConnectionException extends PaymentException {

    public PaymentConnectionException() {
        super(
            HttpStatus.SERVICE_UNAVAILABLE,
            "PAYMENT_CONNECTION_FAILED",
            "결제 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요."
        );
    }
}
