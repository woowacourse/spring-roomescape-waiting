package roomescape.reservation.domain;

public enum Status {
    RESERVED,          // 기존 데이터 호환용
    WAITING,
    PAYMENT_PENDING,   // 결제 대기
    CONFIRMED,         // 결제 완료
    PAYMENT_UNCERTAIN, // 결제 결과 불명확 (read timeout 등)
    ;

    public static Status from(boolean hasConfirmedReservation) {
        if (hasConfirmedReservation) {
            return WAITING;
        }
        return PAYMENT_PENDING;
    }

    public boolean holdsSlot() {
        return this == RESERVED || this == CONFIRMED || this == PAYMENT_PENDING || this == PAYMENT_UNCERTAIN;
    }
}
