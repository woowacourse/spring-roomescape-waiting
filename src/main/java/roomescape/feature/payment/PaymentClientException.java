package roomescape.feature.payment;

/**
 * 토스로부터 정상적인 비즈니스 응답(성공 또는 에러 코드)을 받지 못한 전송 계층 실패.
 *
 * 토스가 "안 된다"고 답한 {@link PaymentException} 과는 구분된다. 이쪽은 "답 자체를 받지 못한" 경우다.
 * 토스 confirm 은 Idempotency-Key 로 안전한 재시도를 보장하므로 이 계열은 재시도 대상이다.
 * 단, 재시도가 모두 소진된 뒤의 처리는 하위 타입에 따라 갈린다.
 */
public abstract class PaymentClientException extends RuntimeException {

    protected PaymentClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
