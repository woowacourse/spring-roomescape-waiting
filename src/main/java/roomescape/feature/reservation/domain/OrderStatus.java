package roomescape.feature.reservation.domain;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    // 결제 승인 결과가 불명확(read timeout 등)해 실패로 단정할 수 없는 상태. 재확인이 필요하다.
    CONFIRMATION_REQUIRED
}
