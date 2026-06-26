package roomescape.domain;

public enum PaymentOrderStatus {
    /** 멱등키 발급 후 승인 전(또는 연결 실패로 토스에 닿지 못함). 같은 키로 재시도 안전 */
    PENDING,
    /** 토스 승인 완료(DONE) */
    CONFIRMED,
    /** 토스가 명시적으로 거절/오류 응답 */
    FAILED,
    /** read timeout 등으로 승인 여부 불명확 — "확인 필요" */
    UNKNOWN
}
