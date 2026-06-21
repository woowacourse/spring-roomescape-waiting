package roomescape.client;

public class PaymentGatewayException extends RuntimeException {
    private final String code;

    public PaymentGatewayException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static class ConnectionFailed extends PaymentGatewayException {
        public ConnectionFailed(Throwable cause) {
            super("PAYMENT_GATEWAY_CONNECTION_FAILED",
                    "결제 승인 요청을 보낼 수 없습니다. 잠시 후 다시 시도해주세요.",
                    cause
            );
        }
    }

    public static class ReadTimeout extends PaymentGatewayException {
        public ReadTimeout(Throwable cause) {
            super(
                    "PAYMENT_GATEWAY_READ_TIMEOUT",
                    "결제 승인 결과를 확인하지 못했습니다. 결제 내역을 확인해주세요.",
                    cause
            );
        }
    }

    public static class Unknown extends PaymentGatewayException {
        public Unknown(Throwable cause) {
            super(
                    "PAYMENT_GATEWAY_UNKNOWN_ERROR",
                    "결제 승인 중 알 수 없는 통신 오류가 발생했습니다.",
                    cause
            );
        }
    }
}
