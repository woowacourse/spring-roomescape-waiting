package roomescape.global.exception;

public class PaymentGatewayConfigurationException extends RuntimeException {

    public PaymentGatewayConfigurationException() {
        super("결제 설정에 문제가 발생했습니다. 관리자에게 문의해주세요.");
    }
}
