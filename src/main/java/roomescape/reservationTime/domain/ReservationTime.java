package roomescape.reservationTime.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.common.exception.BusinessException;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(final Long id, final LocalTime startAt) {
        validateIsNonNull(startAt);

        this.id = id;
        this.startAt = startAt;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("시간 정보는 null 일 수 없습니다.");
        }
    }

    public static ReservationTime createWithoutId(final LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime createWithId(final Long id, final LocalTime startAt) {
        validateIdIsNonNull(id);
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    private static void validateIdIsNonNull(final Long id) {
        if (id == null) {
            throw new BusinessException("시간 id는 null 일 수 없습니다.");
        }
    }

    public ReservationTime assignId(final Long id) {
        return createWithId(id, startAt);
    }

    public boolean isBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }

    public boolean isEqual(final LocalTime time) {
        return startAt.equals(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof ReservationTime that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
