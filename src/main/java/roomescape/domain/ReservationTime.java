package roomescape.domain;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final LocalTime endAt;

    private ReservationTime(final Long id, final LocalTime startAt, final LocalTime endAt) {
        this.id = id;
        validateStartAt(startAt);
        this.startAt = startAt;
        validateEndAt(endAt);
        this.endAt = endAt;
        validateStartAtBeforeEndAt(startAt, endAt);
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("예약 시간 ID는 비워둘 수 없습니다.");
        }
    }

    private static void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 비워둘 수 없습니다.");
        }
    }

    private static void validateEndAt(final LocalTime endAt) {
        if (endAt == null) {
            throw new IllegalArgumentException("종료 시간은 비워둘 수 없습니다.");
        }
    }

    private void validateStartAtBeforeEndAt(final LocalTime startAt, final LocalTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
    }

    public static ReservationTime create(final LocalTime startAt, final LocalTime endAt) {
        return new ReservationTime(
                null,
                startAt,
                endAt
        );
    }

    public static ReservationTime createWithId(
            final Long id,
            final LocalTime startAt,
            final LocalTime endAt
    ) {
        validateId(id);
        return new ReservationTime(id, startAt, endAt);
    }

    public ReservationTime withId(final Long id) {
        validateId(id);
        return new ReservationTime(
                id,
                this.startAt,
                this.endAt
        );
    }

    public boolean isBefore() {
        return startAt.isBefore(LocalTime.now());
    }
}
