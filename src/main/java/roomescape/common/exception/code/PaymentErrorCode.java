package roomescape.common.exception.code;

import org.springframework.http.HttpStatus;

public enum PaymentErrorCode implements ErrorCode {
    AMOUNT_MISMATCH("결제 금액이 주문 금액과 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_PROCESSED("이미 처리된 결제입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_ORDER("중복된 주문번호입니다.", HttpStatus.BAD_REQUEST),
    SESSION_EXPIRED("결제 세션이 만료되었습니다. 다시 시도해 주세요.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("잘못된 결제 요청입니다.", HttpStatus.BAD_REQUEST),
    CARD_REJECTED("카드 결제가 거절되었습니다. 카드사에 문의해 주세요.", HttpStatus.UNPROCESSABLE_ENTITY),
    NOT_FOUND("존재하지 않는 결제입니다.", HttpStatus.NOT_FOUND),
    GATEWAY_CONFIG_ERROR("결제 설정 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    GATEWAY_INTERNAL_ERROR("결제 서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.BAD_GATEWAY),
    UNKNOWN("결제 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    PaymentErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
