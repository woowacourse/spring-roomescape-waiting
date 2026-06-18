package roomescape.order;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    // read timeout 등으로 승인 결과가 불명확한 상태. '실패'가 아니라 '확인 필요' —
    // reaper가 건드리지 않고, 멱등 재시도로 결과를 확정한다.
    NEEDS_CHECK,
    // 결제는 승인됐지만(돈이 나감) 예약을 확정하지 못한 상태. DB 롤백으론 못 되돌리니 환불(보상)이 필요하다 —
    // RefundWorker가 게이트웨이 취소를 호출해 FAILED로 수렴시킨다.
    NEEDS_REFUND
}
