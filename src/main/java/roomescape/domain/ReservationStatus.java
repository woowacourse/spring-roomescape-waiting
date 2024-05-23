package roomescape.domain;

public enum ReservationStatus {
    WAITING,
    RESERVED;

    private static final int RESERVED_NUMBER = 0;

    public static ReservationStatus valueOf(long priority) {
        if (priority == 0L) {
            return RESERVED;
        }
        if (priority > 0L) {
            return WAITING;
        }
        throw new IllegalArgumentException("우선순위는 %d보다는 작을 수 없습니다.".formatted(RESERVED_NUMBER));
    }
}
