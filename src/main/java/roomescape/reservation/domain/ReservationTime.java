package roomescape.reservation.domain;

import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    private void validateStartAt(LocalTime startAt) {
        if (startAt.getSecond() != 0) {
            throw new IllegalArgumentException("[ERROR] 예약 시간은 분 단위까지만 입력해주세요.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
