package roomescape.service.exception;

/**
 * 결제 게이트웨이 승인 실패를 표현하는 애플리케이션 예외. PG사에 종속되지 않으며,
 * 외부 에러 코드 → 이 예외로의 번역은 어댑터(TossPaymentGateway)가 맡는다.
 * 호출부는 code 로 상황별(카드 거절 안내, 키 오류 알람 등) 대응을 분기한다.
 */
public class PaymentException extends RuntimeException {

    private final String code;

    public PaymentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 이미 승인된 결제(중복 승인 시도). 재시도·새로고침 안내.
     */
    public static class AlreadyProcessed extends PaymentException {

        public AlreadyProcessed(String message) {
            super("ALREADY_PROCESSED_PAYMENT", message);
        }
    }

    /**
     * 이미 승인/취소된 중복 주문번호.
     */
    public static class DuplicatedOrder extends PaymentException {

        public DuplicatedOrder(String message) {
            super("DUPLICATED_ORDER_ID", message);
        }
    }

    /**
     * 결제 세션 만료(결제 진행 데이터 없음).
     */
    public static class SessionExpired extends PaymentException {

        public SessionExpired(String message) {
            super("NOT_FOUND_PAYMENT_SESSION", message);
        }
    }

    /**
     * 잘못된 요청(형식 오류, 필수값 누락 등).
     */
    public static class InvalidRequest extends PaymentException {

        public InvalidRequest(String message) {
            super("INVALID_REQUEST", message);
        }
    }

    /**
     * 인증 실패(키 설정 오류). 운영 알람 대상으로 승격.
     */
    public static class GatewayConfig extends PaymentException {

        public GatewayConfig(String message) {
            super("UNAUTHORIZED_KEY", message);
        }
    }

    /**
     * 카드 거절(한도초과/잔액부족). 사용자 안내 대상.
     */
    public static class CardRejected extends PaymentException {

        public CardRejected(String message) {
            super("REJECT_CARD_PAYMENT", message);
        }
    }

    /**
     * 존재하지 않는 결제 건.
     */
    public static class PaymentNotFound extends PaymentException {

        public PaymentNotFound(String message) {
            super("NOT_FOUND_PAYMENT", message);
        }
    }

    /**
     * 게이트웨이 내부 시스템 오류. 재시도 대상.
     */
    public static class Retryable extends PaymentException {

        public Retryable(String message) {
            super("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }
    }
}
