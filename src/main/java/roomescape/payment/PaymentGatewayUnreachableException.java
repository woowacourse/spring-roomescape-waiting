package roomescape.payment;

/**
 * 결제 게이트웨이 연결 단계 실패(연결 거부/연결 타임아웃). 요청이 게이트웨이에 *도달하지 못했으므로*
 * 결제는 일어나지 않았다(확실히 안 됨) — 그대로 안전하게 재시도할 수 있다.
 * 어댑터가 전송 예외(ResourceAccessException)를 이 도메인 의미로 번역해 던진다(ACL 경계).
 */
public class PaymentGatewayUnreachableException extends RuntimeException {
    public PaymentGatewayUnreachableException(String message, Throwable cause) {
        super(message, cause);
    }
}
