package roomescape.payment.infra.client.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public abstract class TossBusinessException extends TossPaymentException {
    protected TossBusinessException(HttpStatusCode status, String code, String message) {
        super(status, code, message);
    }

    /**
     * 400 - 이미 승인된 결제(중복 승인 시도).
     */
    public static class AlreadyProcessed extends TossBusinessException {

        public AlreadyProcessed(String message) {
            super(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", message);
        }

    }

    /**
     * 400 - 이미 승인/취소된 중복 주문번호.
     */
    public static class DuplicatedOrder extends TossBusinessException {

        public DuplicatedOrder(String message) {
            super(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", message);
        }

    }

    /**
     * 400 - 결제 세션 만료(결제 진행 데이터 없음).
     */
    public static class SessionExpired extends TossBusinessException {

        public SessionExpired(String message) {
            super(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", message);
        }

    }

    /**
     * 400 - 잘못된 요청(형식 오류, 필수값 누락 등).
     */
    public static class InvalidRequest extends TossBusinessException {

        public InvalidRequest(String message) {
            super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
        }

    }

    /**
     * 401 - 인증 실패(키 설정 오류). 운영 알람 대상으로 승격.
     */
    public static class GatewayConfig extends TossBusinessException {

        public GatewayConfig(String message) {
            super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", message);
        }

    }

    /**
     * 403 - 카드 거절(한도초과/잔액부족).
     */
    public static class CardRejected extends TossBusinessException {

        public CardRejected(String message) {
            super(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", message);
        }

    }

    /**
     * 404 - 존재하지 않는 결제.
     */
    public static class PaymentNotFound extends TossBusinessException {

        public PaymentNotFound(String message) {
            super(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", message);
        }

    }
}
