package roomescape.domain;

public enum ReservationStatus {
    RESERVED;

    public static String name(ReservationStatus status) {
        if (status == RESERVED) {
            return "예약";
        }
        throw new IllegalStateException("상태가 존재하지 않습니다.");
    }
}
