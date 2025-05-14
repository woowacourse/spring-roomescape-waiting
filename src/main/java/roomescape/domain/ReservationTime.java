package roomescape.domain;

import java.time.LocalTime;

public record ReservationTime(Long id, LocalTime startAt) {

    public ReservationTime {
        validateStartAt(startAt);
    }

    void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("[ERROR] 시작 시간을 반드시 입력해야 합니다. 예시) HH:MM");
        }
    }
}
