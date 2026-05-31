package roomescape.reservationtime.domain;

import java.time.LocalTime;
import lombok.Builder;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final LocalTime finishAt;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private ReservationTime(Long id, LocalTime startAt, LocalTime finishAt) {
        this.id = id;
        this.startAt = startAt;
        this.finishAt = finishAt;
    }

    public static ReservationTime of(LocalTime startAt, LocalTime finishAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }
        if (finishAt == null) {
            throw new IllegalArgumentException("종료 시간은 필수입니다.");
        }
        return ReservationTime.builder()
                .id(null)
                .startAt(startAt)
                .finishAt(finishAt)
                .build();
    }

    public static ReservationTime restore(Long id, LocalTime startAt, LocalTime finishAt) {
        return ReservationTime.builder()
                .id(id)
                .startAt(startAt)
                .finishAt(finishAt)
                .build();
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public LocalTime getFinishAt() {
        return finishAt;
    }
}
