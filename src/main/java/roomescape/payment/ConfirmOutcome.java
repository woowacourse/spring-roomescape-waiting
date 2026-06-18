package roomescape.payment;

/**
 * confirm 호출의 정상 흐름 결과. 토스 거절·연결 실패는 예외로 흐르므로 여기엔 두 결과만 둔다.
 * NEEDS_CHECK: read timeout 등으로 결과가 불명확 — 실패가 아니라 '확인 필요'(사용자가 내역에서 재시도).
 */
public enum ConfirmOutcome {
    CONFIRMED,
    NEEDS_CHECK
}
