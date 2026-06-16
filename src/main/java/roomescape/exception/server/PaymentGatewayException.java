package roomescape.exception.server;

/**
 * 결제 게이트웨이 연동 자체의 문제(우리 쪽 또는 PG사 쪽). 예) 키 설정 오류(UNAUTHORIZED_KEY/INVALID_API_KEY), 토스 내부 오류, 미정의 에러 코드. 사용자 잘못이 아니므로
 * 500으로 로깅·알람 대상.
 * <p>
 * [이월] '키 오류(즉시 알람)'와 '토스 내부오류(재시도 가능)'는 운영 대응이 달라, 재시도 가능 여부에 따른 분리는 mission step2/3에서 다룬다.
 */
public class PaymentGatewayException extends RoomEscapeServerException {

    public PaymentGatewayException(String message) {
        super(message);
    }
}
