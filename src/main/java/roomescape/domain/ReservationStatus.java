package roomescape.domain;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    WAITING(1L),
    RESERVED(0L);

    private final long startIndex;

    ReservationStatus(long startIndex) {
        this.startIndex = startIndex;
    }

    public static ReservationStatus valueOf(long priority) {
        if (priority == RESERVED.startIndex) {
            return RESERVED;
        }
        if (priority >= WAITING.startIndex) {
            return WAITING;
        }
        throw new IllegalArgumentException("우선순위는 %d보다는 작을 수 없습니다.".formatted(RESERVED.startIndex));
    }
}
