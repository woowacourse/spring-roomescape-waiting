package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVED,
    HOLD,     // 예약 대기 상태 (추후 추가될 기능에서 사용)
    REJECTED, // 예약 거절 상태 (추후 추가될 기능에서 사용)
    OVERDUE;  // 예약 만료 상태 (추후 추가될 기능에서 사용)
}
