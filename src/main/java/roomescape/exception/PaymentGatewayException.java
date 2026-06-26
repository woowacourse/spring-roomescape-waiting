package roomescape.exception;

public class PaymentGatewayException extends RoomescapeException {

    private static final String ERROR_CODE = "PAYMENT_GATEWAY_ERROR";
    private static final String MESSAGE = "결제 승인 요청 중 오류가 발생했습니다.";

    public PaymentGatewayException(Throwable cause) {
        super(ERROR_CODE, MESSAGE, cause);
    }
}
