package roomescape.domain.payment;

import org.springframework.http.HttpStatusCode;

public class PaymentException extends RuntimeException {

    private static final String DEFAULT_CODE = "PAYMENT_CONFIRM_FAILED";
    private static final String DEFAULT_MESSAGE = "결제 승인에 실패했습니다.";

    private final HttpStatusCode statusCode;
    private final String code;

    public PaymentException(HttpStatusCode statusCode, String code, String message) {
        super(resolveMessage(message));
        this.statusCode = statusCode;
        this.code = resolveCode(code);
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    private static String resolveCode(String code) {
        if (code == null || code.isBlank()) {
            return DEFAULT_CODE;
        }
        return code;
    }

    private static String resolveMessage(String message) {
        if (message == null || message.isBlank()) {
            return DEFAULT_MESSAGE;
        }
        return message;
    }
}
