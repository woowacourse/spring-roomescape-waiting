package roomescape.domain.payment;

import org.springframework.http.HttpStatus;

public class PaymentResultUnknownException extends PaymentException {

    public PaymentResultUnknownException() {
        super(
            HttpStatus.GATEWAY_TIMEOUT,
            "PAYMENT_RESULT_UNKNOWN",
            "결제 결과를 확인하지 못했습니다. 결제 내역을 확인하거나 다시 시도해주세요."
        );
    }
}
