package roomescape.domain.payment;


public enum PaymentStatus {
    // TODO : 공식문서에 다른 상태들 보고 추가 보충
    PENDING,   // 결제 대기(주문만 저장, 승인 전), 내부 초기 상태
    DONE,
    CANCELED,
    ABORTED,
    UNKNOWN,
    IN_DOUBT;   // read timeout 등 결과 미확정 — 사용자에겐 '확인 필요'. 토스가 주는 값이 아니라 우리가 직접 세팅한다.

    /**
     * 외부 PG사가 준 상태 문자열을 도메인 상태로 번역한다. 모르는 값이 와도 깨지지 않고 UNKNOWN으로 떨어뜨린다(외부 스키마 변화 방어).
     */
    public static PaymentStatus from(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        for (PaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
