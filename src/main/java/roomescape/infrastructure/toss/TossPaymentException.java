package roomescape.infrastructure.toss;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.infrastructure.toss.dto.TossErrorResponse;

public class TossPaymentException extends RuntimeException{

    private final HttpStatusCode status;
    private final String code;

    public TossPaymentException(HttpStatusCode status, String code, String message){
        super(message);
        this.status = status;
        this.code = code;
    }

    public static TossPaymentException of(HttpStatusCode status, TossErrorResponse error) {
        return switch (error.code()){
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(error.message());
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(error.message());
            case "INVALID_REQUEST" -> new InvalidRequest(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.message());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            default -> new TossPaymentException(status, error.code(), error.message());

        };

    }

    public HttpStatusCode getStatus(){
        return status;
    }
    public String getCode(){
        return code;
    }

    /**
     * 400 - 이미 승인된 결제(중복 승인 시도).
     */
    public static class AlreadyProcessed extends  TossPaymentException{

        public AlreadyProcessed(String message){
            super(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", message);
        }
    }

    /**
     * 400 - 이미 승인/취소된 중복 주문번호.
     */
    public static class DuplicatedOrder extends TossPaymentException {

        public DuplicatedOrder(String message) {
            super(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", message);
        }

    }

    /**
     * 400 - 결제 세션 만료(결제 진행 데이터 없음).
     */
    public static class SessionExpired extends TossPaymentException {

        public SessionExpired(String message) {
            super(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", message);
        }

    }

    /**
     * 400 - 잘못된 요청(형식 오류, 필수값 누락 등).
     */
    public static class InvalidRequest extends TossPaymentException {

        public InvalidRequest(String message) {
            super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
        }

    }

    /**
     * 401 - 인증 실패(키 설정 오류). 운영 알람 대상으로 승격.
     */
    public static class GatewayConfig extends TossPaymentException {

        public GatewayConfig(String message) {
            super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", message);
        }

    }

    /**
     * 403 - 카드 거절(한도초과/잔액부족).
     */
    public static class CardRejected extends TossPaymentException {

        public CardRejected(String message) {
            super(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", message);
        }

    }

    /**
     * 404 - 존재하지 않는 결제.
     */
    public static class PaymentNotFound extends TossPaymentException {

        public PaymentNotFound(String message) {
            super(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", message);
        }

    }

    /**
     * 500 - 토스 내부 시스템 오류. 재시도 대상.
     */
    public static class Retryable extends TossPaymentException {

        public Retryable(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }

    }


}
