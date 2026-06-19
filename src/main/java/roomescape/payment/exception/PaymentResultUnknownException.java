package roomescape.payment.exception;

/**
 * 결제 요청은 게이트웨이에 전달됐으나 응답을 받지 못함(read timeout). 게이트웨이 쪽에서 이미 승인됐을 수도
 * 있어 결과가 *불명확*하다 — '결제 실패'로 단정하지 말고, 확인·재시도(멱등키) 가능한 상태로 다뤄야 한다.
 * 어댑터가 전송 예외(RestClientException)를 이 도메인 의미로 번역해 던진다(ACL 경계).
 */
public class PaymentResultUnknownException extends RuntimeException {
    public PaymentResultUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
