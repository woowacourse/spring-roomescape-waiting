package roomescape.payment;

/**
 * 게이트웨이에 조회한 결제 승인 여부(도메인 표현). 토스 상태 문자열(DONE/READY/...)을 어댑터가 이 둘로 번역한다.
 * reconciliation이 결과 불명확(NEEDS_CHECK) 주문을 확정/실패로 수렴시키는 판단 기준이다.
 */
public enum PaymentApprovalStatus {
    APPROVED,
    NOT_APPROVED
}
