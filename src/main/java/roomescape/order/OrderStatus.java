package roomescape.order;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    // read timeout 등으로 승인 결과가 불명확한 상태. '실패'가 아니라 '확인 필요' —
    // reaper가 건드리지 않고, 멱등 재시도로 결과를 확정한다.
    NEEDS_CHECK
}
