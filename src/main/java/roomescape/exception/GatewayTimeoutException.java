package roomescape.exception;

public class GatewayTimeoutException extends RuntimeException {

    public GatewayTimeoutException(Throwable cause) {
        super("결제 서버와 통신 중 오류가 발생했습니다. 잠시 후 내 예약에서 결제 상태를 확인해주세요.", cause);
    }
}
