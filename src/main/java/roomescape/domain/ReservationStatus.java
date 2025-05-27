package roomescape.domain;

public enum ReservationStatus {
    RESERVED, WAITING, CANCELED;

    public static String name(ReservationStatus status) {
        if (status == RESERVED) {
            return "예약";
        }
        if (status == WAITING) {
            return "대기";
        }
        if (status == CANCELED) {
            return "취소";
        }
        throw new IllegalStateException("상태가 존재하지 않습니다.");
    }
}
